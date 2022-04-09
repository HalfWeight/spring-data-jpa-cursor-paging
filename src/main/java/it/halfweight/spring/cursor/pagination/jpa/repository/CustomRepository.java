package it.halfweight.spring.cursor.pagination.jpa.repository;


import it.halfweight.spring.cursor.pagination.jpa.domain.CursorPageable;
import it.halfweight.spring.cursor.pagination.jpa.domain.CursorPaginationSlice;
import it.halfweight.spring.cursor.pagination.jpa.domain.Projection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface CustomRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Returns all entities matching the given {@link Specification}.
     *
     * @param spec can be {@literal null}.
     * @param pageable must not be {@literal null}.
     * @return never {@literal null}.
     */
    List<T> findAllBy(@Nullable Specification<T> spec, Pageable pageable);

    /** TODO doc
     * Returns all entities matching the given {@link Specification}.
     *
     * @param spec can be {@literal null}.
     * @param cursorPageable must not be {@literal null}.
     * @return never {@literal null}.
     */
    CursorPaginationSlice<T> findAllBy(@Nullable Specification<T> spec, CursorPageable cursorPageable);

    /**
     * Returns all entities matching the given {@link Specification}, applying projection defined by given projectionClass.
     * <P>
     * ProjectionClass should have only one constructor with parameters, otherwise should have only one constructor annotated
     * with {@link it.halfweight.spring.cursor.pagination.jpa.annotation.ProjectionCreator}. Constructor parameters should match entity T field names,
     * otherwise should be annotated with {@link it.halfweight.spring.cursor.pagination.jpa.annotation.SelectPath} annotation to override field path.
     * Please also note that constructor parameter names without Param annotation will work only in case "-parameters" java
     * compiler argument is used.
     *
     * @param spec can be {@literal null}.
     * @param projectionClass must not be {@literal null}.
     * @return never {@literal null}.
     */
    <S> List<S> findAllProjection(@Nullable Specification<T> spec, Class<S> projectionClass);

    /**
     * Returns all entities matching the given {@link Specification}, applying projection to projectionClass defined by
     * given {@link Projection} function.
     *
     * @param spec can be {@literal null}.
     * @param projectionClass must not be {@literal null}.
     * @param projection must not be {@literal null}.
     * @return never {@literal null}.
     */
    <S> List<S> findAllProjection(@Nullable Specification<T> spec, Class<S> projectionClass, Projection<T, S> projection);

    /**
     * Returns all entities matching the given {@link Specification}, applying projection defined by given projectionClass.
     * <P>
     * ProjectionClass should have only one constructor with parameters, otherwise should have only one constructor annotated
     * with {@link it.halfweight.spring.cursor.pagination.jpa.annotation.ProjectionCreator}. Constructor parameters should match entity T field names,
     * otherwise should be annotated with {@link it.halfweight.spring.cursor.pagination.jpa.annotation.SelectPath} annotation to override field path.
     * Please also note that constructor parameter names without Param annotation will work only in case "-parameters" java
     * compiler argument is used.
     *
     * @param spec can be {@literal null}.
     * @param pageable must not be {@literal null}.
     * @param projectionClass must not be {@literal null}.
     * @return never {@literal null}.
     */
    <S> List<S> findAllProjection(@Nullable Specification<T> spec, Pageable pageable, Class<S> projectionClass);

    /**
     * Returns all entities matching the given {@link Specification}, applying projection to projectionClass defined by
     * given {@link Projection} function.
     *
     * @param spec can be {@literal null}.
     * @param pageable must not be {@literal null}.
     * @param projectionClass must not be {@literal null}.
     * @param projection must not be {@literal null}.
     * @return never {@literal null}.
     */
    <S> List<S> findAllProjection(@Nullable Specification<T> spec, Pageable pageable, Class<S> projectionClass, Projection<T, S> projection);

    /**
     * Returns all entities matching the given {@link Specification}, applying projection defined by given projectionClass.
     * <P>
     * ProjectionClass should have only one constructor with parameters, otherwise should have only one constructor annotated
     * with {@link it.halfweight.spring.cursor.pagination.jpa.annotation.ProjectionCreator}. Constructor parameters should match entity T field names,
     * otherwise should be annotated with {@link it.halfweight.spring.cursor.pagination.jpa.annotation.SelectPath} annotation to override field path.
     * Please also note that constructor parameter names without Param annotation will work only in case "-parameters" java
     * compiler argument is used.
     *
     * @param spec can be {@literal null}.
     * @param sort must not be {@literal null}.
     * @param projectionClass must not be {@literal null}.
     * @return never {@literal null}.
     */
    <S> List<S> findAllProjection(@Nullable Specification<T> spec, Sort sort, Class<S> projectionClass);

    /**
     * Returns all entities matching the given {@link Specification}, applying projection to projectionClass defined by
     * given {@link Projection}.
     *
     * @param spec can be {@literal null}.
     * @param sort must not be {@literal null}.
     * @param projectionClass must not be {@literal null}.
     * @param projection must not be {@literal null}.
     * @return never {@literal null}.
     */
    <S> List<S> findAllProjection(@Nullable Specification<T> spec, Sort sort, Class<S> projectionClass, Projection<T, S> projection);

    /**
     * Returns a {@link Page} of entities matching the given {@link Specification}, applying projection defined by given projectionClass.
     * <P>
     * ProjectionClass should have only one constructor with parameters, otherwise should have only one constructor annotated
     * with {@link it.halfweight.spring.cursor.pagination.jpa.annotation.ProjectionCreator}. Constructor parameters should match entity T field names,
     * otherwise should be annotated with {@link it.halfweight.spring.cursor.pagination.jpa.annotation.SelectPath} annotation to override field path.
     * Please also note that constructor parameter names without Param annotation will work only in case -parameters java compiler
     * argument is used.
     *
     * @param spec can be {@literal null}.
     * @param pageable must not be {@literal null}.
     * @param projectionClass must not be {@literal null}.
     * @return never {@literal null}.
     */
    <S> Page<S> findAllProjectionPaged(@Nullable Specification<T> spec, Pageable pageable, Class<S> projectionClass);

    /**
     * Returns a {@link Page} of entities matching the given {@link Specification}, applying projection to projectionClass
     * defined by given {@link Projection}.
     *
     * @param spec can be {@literal null}.
     * @param pageable must not be {@literal null}.
     * @param projectionClass must not be {@literal null}.
     * @param projection must not be {@literal null}.
     * @return never {@literal null}.
     */
    <S> Page<S> findAllProjectionPaged(@Nullable Specification<T> spec, Pageable pageable, Class<S> projectionClass, Projection<T, S> projection);

    /**
     * Returns all entity ids matching the given {@link Specification}.
     *
     * @param spec can be {@literal null}.
     * @return never {@literal null}.
     */
    List<ID> findAllIds(@Nullable Specification<T> spec);

}
