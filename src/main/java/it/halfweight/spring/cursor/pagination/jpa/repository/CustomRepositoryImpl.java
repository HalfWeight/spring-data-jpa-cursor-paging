package it.halfweight.spring.cursor.pagination.jpa.repository;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import it.halfweight.spring.cursor.pagination.jpa.annotation.ProjectionCreator;
import it.halfweight.spring.cursor.pagination.jpa.annotation.SelectPath;
import it.halfweight.spring.cursor.pagination.jpa.domain.CursorPageable;
import it.halfweight.spring.cursor.pagination.jpa.exception.CursorPaginationException;
import it.halfweight.spring.cursor.pagination.jpa.domain.CursorPaginationSlice;
import it.halfweight.spring.cursor.pagination.jpa.domain.Projection;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

public class CustomRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements CustomRepository<T, ID> {

    @SuppressWarnings("rawtypes")
    private final Map<Class, Projection> projectionMap = new ConcurrentHashMap<>();
    private final EntityManager em;
    private final JpaEntityInformation<T, ?> entityInformation;
    private static final String UNSERSCORE = "_";
    private static final String EQUAL = "=";
    private static final String SEMICOLON = ";";


    public CustomRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.em = entityManager;
        this.entityInformation = entityInformation;
    }

    @Override
    public List<T> findAllBy(Specification<T> spec, Pageable pageable) {
        TypedQuery<T> query = getQuery(spec, pageable);
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public CursorPaginationSlice<T> findAllBy(Specification<T> spec, CursorPageable cursorPageable) {
        Sort sort = cursorPageable.getSort();
        Preconditions.checkArgument(sort != null, "Can't execute a cursor find without a sort");
        String hash = getHash(cursorPageable);
        spec = enrichSpecificationWithContinuationToken(spec, sort, cursorPageable.getContinuationToken(), hash);

        TypedQuery<T> query = getQuery(spec, sort);
        query.setMaxResults(cursorPageable.getSize() + 1);
        List<T> results = query.getResultList();
        boolean hasNext = results.size() > cursorPageable.getSize();
        String nextToken = null;

        if (hasNext) {
            results = results.subList(0, results.size() - 1);
            nextToken = computeNextToken(results, sort, hash);
        }

        return new CursorPaginationSlice(results, getSize(spec, cursorPageable), nextToken);
    }

    @Override
    public <S> List<S> findAllProjection(Specification<T> spec, Class<S> projectionClass) {
        return findAllProjection(spec, projectionClass, getProjection(projectionClass));
    }

    @Override
    public <S> List<S> findAllProjection(Specification<T> spec, Class<S> projectionClass, Projection<T, S> projection) {
        return getProjectionQuery(spec, Sort.unsorted(), projectionClass, projection).getResultList();
    }

    @Override
    public <S> List<S> findAllProjection(Specification<T> spec, Pageable pageable, Class<S> projectionClass) {
        return findAllProjection(spec, pageable, projectionClass, getProjection(projectionClass));
    }

    @Override
    public <S> List<S> findAllProjection(Specification<T> spec, Pageable pageable, Class<S> projectionClass, Projection<T, S> projection) {
        TypedQuery<S> query = getProjectionQuery(spec, pageable, projectionClass, projection);
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public <S> List<S> findAllProjection(Specification<T> spec, Sort sort, Class<S> projectionClass) {
        return findAllProjection(spec, sort, projectionClass, getProjection(projectionClass));
    }

    @Override
    public <S> List<S> findAllProjection(Specification<T> spec, Sort sort, Class<S> projectionClass, Projection<T, S> projection) {
        return getProjectionQuery(spec, sort, projectionClass, projection).getResultList();
    }

    @Override
    public <S> Page<S> findAllProjectionPaged(Specification<T> spec, Pageable pageable, Class<S> projectionClass) {
        return findAllProjectionPaged(spec, pageable, projectionClass, getProjection(projectionClass));
    }

    public <S> Page<S> findAllProjectionPaged(Specification<T> spec, Pageable pageable, Class<S> projectionClass, Projection<T, S> projection) {
        Class<T> domainClass = getDomainClass();
        TypedQuery<S> query = getProjectionQuery(spec, pageable, projectionClass, projection);
        return pageable.isUnpaged() ? new PageImpl<>(query.getResultList())
                : readProjectionPage(query, domainClass, pageable, spec);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ID> findAllIds(Specification<T> spec) {
        return findAllProjection(spec, (Class<ID>) entityInformation.getIdType(), (root, cb) -> (Path<ID>) root.get(entityInformation.getRequiredIdAttribute()));
    }

    protected <S> TypedQuery<S> getProjectionQuery(@Nullable Specification<T> spec, Pageable pageable, Class<S> projectionClass, Projection<T, S> projection) {
        Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        return getProjectionQuery(spec, sort, projectionClass, projection);
    }

    protected <S> TypedQuery<S> getProjectionQuery(@Nullable Specification<T> spec, Sort sort, Class<S> projectionClass, Projection<T, S> projection) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<S> query = cb.createQuery(projectionClass);

        Root<T> root = query.from(getDomainClass());
        query.select(projection.toSelection(root, cb));

        ofNullable(spec).map(s -> s.toPredicate(root, query, cb))
                .ifPresent(query::where);

        if (sort.isSorted()) {
            query.orderBy(toOrders(sort, root, cb));
        }

        return applyRepositoryMethodMetadata(em.createQuery(query));
    }

    protected <S> Page<S> readProjectionPage(TypedQuery<S> query, final Class<T> domainClass, Pageable pageable, @Nullable Specification<T> spec) {
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return PageableExecutionUtils.getPage(query.getResultList(), pageable,
                () -> executeCountQuery(getCountQuery(spec, domainClass)));
    }

    protected <S> TypedQuery<S> getProjectionQuery(@Nullable Specification<T> spec, Class<S> resultClass, Projection<T, S> projection, Pageable pageable) {
        Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        return getProjectionQuery(spec, sort, resultClass, projection);
    }

    private <S> TypedQuery<S> applyRepositoryMethodMetadata(TypedQuery<S> query) {
        CrudMethodMetadata metadata = super.getRepositoryMethodMetadata();
        if (metadata == null) {
            return query;
        }
        LockModeType type = metadata.getLockModeType();
        return type == null ? query : query.setLockMode(type);
    }

    @SuppressWarnings("unchecked")
    private <S> Projection<T, S> getProjection(Class<S> projectionClass) {
        return projectionMap.computeIfAbsent(projectionClass, ClassProjection::new);
    }

    private static Long executeCountQuery(TypedQuery<Long> query) {
        Assert.notNull(query, "TypedQuery must not be null!");
        List<Long> totals = query.getResultList();
        long total = 0L;
        for (Long element : totals) {
            total += element == null ? 0 : element;
        }
        return total;
    }

    public static class ClassProjection<E, S> implements Projection<E, S> {
        private final Class<S> projectionClass;
        private final List<ProjectionSelection> projectionSelections;

        public ClassProjection(Class<S> projectionClass) {
            this.projectionClass = projectionClass;
            Constructor<?> constructor = getProjectionConstructor(projectionClass);
            this.projectionSelections = Arrays.stream(constructor.getParameters())
                    .map(this::getProjectionSelection)
                    .collect(toList());
        }

        @Override
        public Selection<S> toSelection(Root<E> root, CriteriaBuilder cb) {
            return cb.construct(projectionClass, projectionSelections.stream()
                    .map(pjSelection -> getPath(root, pjSelection.path).as(pjSelection.type))
                    .toArray(Selection[]::new));
        }

        private ProjectionSelection getProjectionSelection(Parameter parameter) {
            SelectPath selectPath = parameter.getAnnotation(SelectPath.class);
            if (selectPath != null) {
                return new ProjectionSelection(parameter.getType(), selectPath.value().split("\\."));
            }
            return new ProjectionSelection(parameter.getType(), parameter.getName());
        }

        private Path<?> getPath(Root<E> root, String[] pathArray) {
            Path<?> path = root.get(pathArray[0]);
            for (int i = 1; i < pathArray.length; i++) {
                path = path.get(pathArray[i]);
            }
            return path;
        }

        private Constructor<?> getProjectionConstructor(Class<S> projectionClass) {
            Constructor<?>[] constructorsWithParams = Arrays.stream(projectionClass.getDeclaredConstructors())
                    .filter(c -> c.getParameterCount() > 0).toArray(Constructor[]::new);
            if (constructorsWithParams.length == 1) {
                return constructorsWithParams[0];
            }
            if (constructorsWithParams.length == 0) {
                throw new IllegalArgumentException("projection " + projectionClass + " should have a constructor with parameters");
            }
            Constructor<?>[] constructorsWithAnnotation = Arrays.stream(constructorsWithParams)
                    .filter(c -> c.getAnnotation(ProjectionCreator.class) != null).toArray(Constructor[]::new);
            if (constructorsWithAnnotation.length == 0) {
                throw new IllegalArgumentException("projection " + projectionClass + " should have exactly one constructor with parameters");
            } else if (constructorsWithAnnotation.length > 1) {
                throw new IllegalArgumentException("projection " + projectionClass + " should have exactly one constructor with @ProjectionCreator annotation");
            }
            return constructorsWithAnnotation[0];
        }

        public static class ProjectionSelection {
            private final Class<?> type;
            private final String[] path;

            public ProjectionSelection(Class<?> type, String... path) {
                this.type = type;
                this.path = path;
            }
        }
    }

    protected ContinuationTokenInfo decrypt(String strToDecrypt) {
        try {
            byte[] decrypted = Base64Utils.decodeFromUrlSafeString(strToDecrypt);
            String token = new String(decrypted);
            String[] splitToken = token.split(UNSERSCORE);
            String prevSortHashed = splitToken[0];
            Map<String, String> continuationToken = Splitter.on(SEMICOLON).withKeyValueSeparator(EQUAL).split(splitToken[1]);
            return new ContinuationTokenInfo(prevSortHashed, continuationToken);
        } catch (Exception e) {
            throw new CursorPaginationException("Unable to decrypt " + strToDecrypt, e);
        }
    }

    protected String encrypt(String hash, Map<String, String> mapToEncrypt) {
        try {
            String enc = Joiner.on(SEMICOLON).withKeyValueSeparator(EQUAL).join(mapToEncrypt);
            return Base64Utils.encodeToUrlSafeString((hash + UNSERSCORE + enc).getBytes());
        } catch (Exception e) {
            throw new CursorPaginationException("Unable to encrypt " + mapToEncrypt, e);
        }
    }

    protected String getHash(CursorPageable pageRequest) {
        return DigestUtils.md5DigestAsHex(pageRequest.getSort().toString().getBytes());
    }

    protected Comparable getStartValue(String propertyName, String value) {
        Field field = ReflectionUtils.findField(getDomainClass(), propertyName);
        Preconditions.checkArgument(field != null, "Field not found by reflection fieldName: " + propertyName + " class:" + getDomainClass());

        Object startingPoint = ConvertUtils.convert(value, field.getType());
        Preconditions.checkArgument(Comparable.class.isAssignableFrom(startingPoint.getClass()), "Cannot use a non comparable field");
        return (Comparable) startingPoint;
    }

    protected String getLastValue(T lastItem, String propertyName) {
        try {
            Field field = ReflectionUtils.findField(lastItem.getClass(), propertyName);
            Preconditions.checkArgument(field != null, "Field not found by reflection fieldName: " + propertyName + " class:" + getDomainClass());
            field.setAccessible(true);
            Object objectValue = field.get(lastItem);
            return (String) ConvertUtils.convert(objectValue, String.class);
        } catch (IllegalAccessException e) {
            throw new CursorPaginationException("Unable to get field from " + entityInformation.getJavaType() + " of name " + propertyName, e);
        }
    }

    private Specification<T> enrichSpecificationWithContinuationToken(Specification<T> spec, Sort sort, String continuationToken, String currentSortHash) {
        if (Strings.isNullOrEmpty(continuationToken)) {
            return spec;
        }
        ContinuationTokenInfo continuationTokenInfo = decrypt(continuationToken);
        String prevHash = continuationTokenInfo.sortHash;
        Map<String, String> continuationTokenMap = continuationTokenInfo.continuationToken;
        Preconditions.checkArgument(currentSortHash.equals(prevHash), "Can't modify sort filter when using a continuationToken");


        if (continuationTokenMap != null && !continuationTokenMap.isEmpty()) {
            spec = ofNullable(spec).orElseGet(() -> (root, query, cb) -> cb.and());
            spec = spec.and(getContinuationPredicate(continuationTokenMap, sort));
        }
        return spec;
    }

    private Specification<T> getContinuationPredicate(Map<String, String> continuationToken, Sort sort) {
        return (root, query, cb) -> {

            List<Predicate> equalPredicates = new ArrayList<>();
            List<Predicate> predicates = new ArrayList<>();

            for (Map.Entry<String, String> entry : continuationToken.entrySet()) {
                Sort.Order order = sort.getOrderFor(entry.getKey());
                Predicate p;
                Comparable startingValue = getStartValue(order.getProperty(), entry.getValue());
                if (order.getDirection() == Sort.Direction.DESC) {
                    p = cb.lessThan(root.get(entry.getKey()), startingValue);
                } else {
                    p = cb.greaterThan(root.get(entry.getKey()), startingValue);
                }

                p = cb.and(p);


                if (!equalPredicates.isEmpty()) {
                    p = cb.and(p, cb.and(equalPredicates.toArray(new Predicate[0])));
                }

                equalPredicates.add(cb.equal(root.get(entry.getKey()), startingValue));
                predicates.add(p);
            }
            return predicates.stream().reduce(cb::or).orElse(cb.conjunction());
        };
    }

    /**
     * token has the following structure:
     * ${sortHash}_${continuationToken}
     * <p>
     * The prevSortHash: This field stores the sort hashed
     * continuationToken: stores the field name and field value of the last retrieved element per each sorted property
     * in this structure: ${fieldName1}=${fieldValue1};${fieldName2}=${fieldValue2};${fieldName3}=${fieldValue3}
     */
    private String computeNextToken(List<T> results, Sort sort, String currentSortHash) {
        T item = Iterables.getLast(results);
        Map<String, String> map = sort.stream().collect(Collectors.toMap(Sort.Order::getProperty, _sort -> getLastValue(item, _sort.getProperty())));
        return encrypt(currentSortHash, map);
    }

    private int getSize(Specification<T> specification, CursorPageable cursorPageable) {
        if (!Strings.isNullOrEmpty(cursorPageable.getContinuationToken())) {
            return cursorPageable.getSize();
        }
        return executeCountQuery(getCountQuery(specification, getDomainClass())).intValue();
    }

    private static class ContinuationTokenInfo {

        /**
         * This field stores the sort (hashed) used on a previous call
         */
        public final String sortHash;

        /**
         * This map contains:
         * - key: the property (field) name
         * - value: last retrieved value, the starting point for the current call
         * <p>
         * Example:
         * Given a table with the id: 1, 2, 3, 4, 5
         * And assuming that the first request was: get the first 3 rows sorted by id asc
         * In the second request, this map contains:
         * key: id
         * value: 3
         */
        public final Map<String, String> continuationToken;

        public ContinuationTokenInfo(String sortHash, Map<String, String> continuationToken) {
            this.sortHash = sortHash;
            this.continuationToken = continuationToken;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("sortHash", sortHash)
                    .add("continuationToken", continuationToken)
                    .toString();
        }
    }

}
