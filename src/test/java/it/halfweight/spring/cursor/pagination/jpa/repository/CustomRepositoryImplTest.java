package it.halfweight.spring.cursor.pagination.jpa.repository;

import it.halfweight.spring.cursor.pagination.jpa.annotation.ProjectionCreator;
import it.halfweight.spring.cursor.pagination.jpa.annotation.SelectPath;
import it.halfweight.spring.cursor.pagination.jpa.domain.CursorPageRequest;
import it.halfweight.spring.cursor.pagination.jpa.domain.CursorPaginationSlice;
import it.halfweight.spring.cursor.pagination.jpa.domain.Money;
import it.halfweight.spring.cursor.pagination.jpa.domain.TestChildEntity;
import it.halfweight.spring.cursor.pagination.jpa.domain.TestEntity;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.AbstractConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestCustomRepositorySetup.class)
public class CustomRepositoryImplTest {

    @Autowired
    TestRepository testRepository;

    @BeforeEach
    public void setUp() {
        testRepository.deleteAll();
    }

    @Test
    public void findAllProjection() {
        TestEntity testEntity = testRepository.save(new TestEntity("stringField", Money.money(10D, Currency.getInstance("EUR"))));
        TestEntity testEntity2 = testRepository.save(new TestEntity("stringField2", Money.money(20D, Currency.getInstance("EUR"))));

        List<TestEntityProjectionSingleConstructor> pjList = testRepository.findAllProjection(null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")),
                TestEntityProjectionSingleConstructor.class);

        assertThat(pjList.size(), is(1));
        assertThat(pjList.get(0).id, is(testEntity.id));
        assertThat(pjList.get(0).moneyField, is(testEntity.moneyField));

        List<TestEntityProjectionEmptyConstructor> pjEmptyConstructorList = testRepository.findAllProjection(null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")),
                TestEntityProjectionEmptyConstructor.class);

        assertThat(pjEmptyConstructorList.size(), is(1));
        assertThat(pjEmptyConstructorList.get(0).id, is(testEntity.id));
        assertThat(pjEmptyConstructorList.get(0).moneyField, is(testEntity.moneyField));

        List<TestEntityProjectionMultipleConstructor> pjMultipleConstructorList = testRepository.findAllProjection(null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")),
                TestEntityProjectionMultipleConstructor.class);

        assertThat(pjMultipleConstructorList.size(), is(1));
        assertThat(pjMultipleConstructorList.get(0).id, is(testEntity.id));
        assertThat(pjMultipleConstructorList.get(0).moneyField, is(testEntity.moneyField));
        assertThat(pjMultipleConstructorList.get(0).stringField, nullValue());

        assertThatThrownBy(() -> testRepository.findAllProjection(null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")),
                TestEntityProjectionNoConstructorWithParameter.class)).hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void findAllProjectionPaged() {
        TestEntity testEntity = testRepository.save(new TestEntity("stringField", Money.money(10D, Currency.getInstance("EUR"))));
        TestEntity testEntity2 = testRepository.save(new TestEntity("stringField2", Money.money(20D, Currency.getInstance("EUR"))));

        Page<TestEntityProjectionSingleConstructor> pjPage = testRepository.findAllProjectionPaged(null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")),
                TestEntityProjectionSingleConstructor.class);

        assertThat(pjPage.getTotalElements(), is(2L));
        assertThat(pjPage.getTotalPages(), is(2));
        assertThat(pjPage.getContent().size(), is(1));
        assertThat(pjPage.getContent().get(0).id, is(testEntity.id));
        assertThat(pjPage.getContent().get(0).moneyField, is(testEntity.moneyField));
    }

    @Test
    public void findAllIds() {
        TestEntity testEntity = testRepository.save(new TestEntity("stringField", Money.money(10D, Currency.getInstance("EUR"))));
        TestEntity testEntity2 = testRepository.save(new TestEntity("stringField2", Money.money(20D, Currency.getInstance("EUR"))));

        List<Long> idsProjection = testRepository.findAllProjection(null, Long.class, (root, cb) -> root.get("id"));
        assertThat(idsProjection.size(), is(2));
        assertThat(idsProjection, containsInAnyOrder(testEntity.id, testEntity2.id));

        List<Long> ids = testRepository.findAllIds(null);
        assertThat(ids.size(), is(2));
        assertThat(ids, containsInAnyOrder(testEntity.id, testEntity2.id));
    }

    @Test
    public void testSpecificationFilter() {
        TestEntity testEntity = testRepository.save(new TestEntity("stringField", Money.money(10D, Currency.getInstance("EUR"))));
        TestEntity testEntity2 = testRepository.save(new TestEntity("stringField2", Money.money(20D, Currency.getInstance("EUR"))));

        List<TestEntityProjectionSingleConstructor> result = testRepository.findAllProjection(
                (r, q, cb) -> cb.equal(r.get("stringField"), "stringField2"),
                TestEntityProjectionSingleConstructor.class);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).id, is(testEntity2.id));
    }

    @Test
    public void testSelectPath() {
        TestEntity testEntity = testRepository.save(new TestEntity("stringField", Money.money(10D, Currency.getInstance("EUR")),
                new TestChildEntity("childStringField")));

        List<TestEntityProjectionWithSelectPath> result = testRepository.findAllProjection(null, TestEntityProjectionWithSelectPath.class);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).id, is(testEntity.id));
        assertThat(result.get(0).stringField, is(testEntity.stringField));
        assertThat(result.get(0).childStringField, is(testEntity.childEntity.stringField));
    }

    @Test
    public void testCursorPaginationSortByDateDesc() {
        TestEntity testEntity1 = testRepository.save(new TestEntity("First", Instant.now().minus(20, ChronoUnit.MINUTES)));
        TestEntity testEntity2 = testRepository.save(new TestEntity("Second", Instant.now().minus(10, ChronoUnit.MINUTES)));
        TestEntity testEntity3 = testRepository.save(new TestEntity("Third", Instant.now().minus(5, ChronoUnit.MINUTES)));
        TestEntity testEntity4 = testRepository.save(new TestEntity("Four", Instant.now().minus(4, ChronoUnit.MINUTES)));
        TestEntity testEntity5 = testRepository.save(new TestEntity("Five", Instant.now().minus(3, ChronoUnit.MINUTES)));

        CursorPaginationSlice<TestEntity> result = testRepository.findAllBy(null, CursorPageRequest.of(1, Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id"))));
        assertThat(result.hasNext(), equalTo(true));
        assertThat(result.getContent().size(), equalTo(1));
        assertThat(result.getContinuationToken(), notNullValue());
        assertThat(result.getContent().get(0), equalTo(testEntity5));

        result = testRepository.findAllBy(null, CursorPageRequest.of(result.getContinuationToken(), 2, Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id"))));
        assertThat(result.hasNext(), equalTo(true));
        assertThat(result.getContent().size(), equalTo(2));
        assertThat(result.getContinuationToken(), notNullValue());
        assertThat(result.getContent(), contains(testEntity4, testEntity3));

        result = testRepository.findAllBy(null, CursorPageRequest.of(result.getContinuationToken(), 2, Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id"))));
        assertThat(result.hasNext(), equalTo(false));
        assertThat(result.getContent().size(), equalTo(2));
        assertThat(result.getContinuationToken(), nullValue());
        assertThat(result.getContent(), contains(testEntity2, testEntity1));
    }

    @Test
    public void testCursorPaginationSortByDateAsc() {
        TestEntity testEntity1 = testRepository.save(new TestEntity("First", Instant.now().minus(20, ChronoUnit.MINUTES)));
        TestEntity testEntity2 = testRepository.save(new TestEntity("Second", Instant.now().minus(10, ChronoUnit.MINUTES)));
        TestEntity testEntity3 = testRepository.save(new TestEntity("Third", Instant.now().minus(5, ChronoUnit.MINUTES)));
        TestEntity testEntity4 = testRepository.save(new TestEntity("Four", Instant.now().minus(4, ChronoUnit.MINUTES)));
        TestEntity testEntity5 = testRepository.save(new TestEntity("Five", Instant.now().minus(3, ChronoUnit.MINUTES)));

        CursorPaginationSlice<TestEntity> result = testRepository.findAllBy(null, CursorPageRequest.of(1, Sort.by(Sort.Order.asc("date"), Sort.Order.asc("id"))));
        assertThat(result.hasNext(), equalTo(true));
        assertThat(result.getContent().size(), equalTo(1));
        assertThat(result.getContinuationToken(), notNullValue());
        assertThat(result.getContent().get(0), equalTo(testEntity1));

        result = testRepository.findAllBy(null, CursorPageRequest.of(result.getContinuationToken(), 2, Sort.by(Sort.Order.asc("date"), Sort.Order.asc("id"))));
        assertThat(result.hasNext(), equalTo(true));
        assertThat(result.getContent().size(), equalTo(2));
        assertThat(result.getContinuationToken(), notNullValue());
        assertThat(result.getContent(), contains(testEntity2, testEntity3));

        result = testRepository.findAllBy(null, CursorPageRequest.of(result.getContinuationToken(), 2, Sort.by(Sort.Order.asc("date"), Sort.Order.asc("id"))));
        assertThat(result.hasNext(), equalTo(false));
        assertThat(result.getContent().size(), equalTo(2));
        assertThat(result.getContinuationToken(), nullValue());
        assertThat(result.getContent(), contains(testEntity4, testEntity5));
    }

    @Test
    public void testCursorPaginationChangingSorting() {
        TestEntity testEntity1 = testRepository.save(new TestEntity("First", Instant.now().minus(20, ChronoUnit.MINUTES)));
        TestEntity testEntity2 = testRepository.save(new TestEntity("Second", Instant.now().minus(10, ChronoUnit.MINUTES)));
        TestEntity testEntity3 = testRepository.save(new TestEntity("Third", Instant.now().minus(5, ChronoUnit.MINUTES)));
        TestEntity testEntity4 = testRepository.save(new TestEntity("Four", Instant.now().minus(4, ChronoUnit.MINUTES)));
        TestEntity testEntity5 = testRepository.save(new TestEntity("Five", Instant.now().minus(3, ChronoUnit.MINUTES)));

        CursorPaginationSlice<TestEntity> result = testRepository.findAllBy(null, CursorPageRequest.of(1, Sort.by(Sort.Order.asc("date"), Sort.Order.asc("id"))));
        assertThat(result.hasNext(), equalTo(true));
        assertThat(result.getContent().size(), equalTo(1));
        assertThat(result.getContinuationToken(), notNullValue());
        assertThat(result.getContent().get(0), equalTo(testEntity1));

        assertThatThrownBy(() -> testRepository.findAllBy(null, CursorPageRequest.of(result.getContinuationToken(), 2, Sort.by(Sort.Order.desc("date"), Sort.Order.asc("id")))))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCursorSameDateFallbackOnId() {
        Instant now = Instant.now();
        Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);
        Instant twoMinutesAgo = Instant.now().minus(2, ChronoUnit.MINUTES);
        TestEntity testEntity1 = testRepository.save(new TestEntity("First", now)); // id 1
        TestEntity testEntity2 = testRepository.save(new TestEntity("Second", oneMinuteAgo)); // id 2
        TestEntity testEntity3 = testRepository.save(new TestEntity("Third", now)); // id 3
        TestEntity testEntity4 = testRepository.save(new TestEntity("Four", oneMinuteAgo)); // id 4
        TestEntity testEntity5 = testRepository.save(new TestEntity("Five", now)); // id 5
        TestEntity testEntity6 = testRepository.save(new TestEntity("Six", twoMinutesAgo)); // id 6

        CursorPaginationSlice<TestEntity> result = testRepository.findAllBy(null, CursorPageRequest.of(2, Sort.by(Sort.Order.desc("date"), Sort.Order.asc("id"))));
        assertThat(result.hasNext(), equalTo(true));
        assertThat(result.getContent().size(), equalTo(2));
        assertThat(result.getContinuationToken(), notNullValue());
        assertThat(result.getContent(), contains(testEntity1, testEntity3));

        result = testRepository.findAllBy(null, CursorPageRequest.of(result.getContinuationToken(), 2, Sort.by(Sort.Order.desc("date"), Sort.Order.asc("id"))));
        assertThat(result.hasNext(), equalTo(true));
        assertThat(result.getContent().size(), equalTo(2));
        assertThat(result.getContinuationToken(), notNullValue());
        assertThat(result.getContent(), contains(testEntity5, testEntity2));

        result = testRepository.findAllBy(null, CursorPageRequest.of(result.getContinuationToken(), 2, Sort.by(Sort.Order.desc("date"), Sort.Order.asc("id"))));
        assertThat(result.hasNext(), equalTo(false));
        assertThat(result.getContent().size(), equalTo(2));
        assertThat(result.getContinuationToken(), nullValue());
        assertThat(result.getContent(), contains(testEntity4, testEntity6));
    }

    public static class TestEntityProjectionSingleConstructor {
        Long id;
        Money moneyField;

        public TestEntityProjectionSingleConstructor(Long id, Money moneyField) {
            this.id = id;
            this.moneyField = moneyField;
        }
    }

    public static class TestEntityProjectionEmptyConstructor {
        Long id;
        Money moneyField;

        public TestEntityProjectionEmptyConstructor() {
        }

        public TestEntityProjectionEmptyConstructor(Long id, Money moneyField) {
            this.id = id;
            this.moneyField = moneyField;
        }
    }

    public static class TestEntityProjectionMultipleConstructor {
        Long id;
        String stringField;
        Money moneyField;

        public TestEntityProjectionMultipleConstructor() {
        }

        @ProjectionCreator
        public TestEntityProjectionMultipleConstructor(Long id, Money moneyField) {
            this.id = id;
            this.moneyField = moneyField;
        }

        public TestEntityProjectionMultipleConstructor(Long id, String stringField, Money moneyField) {
            this.id = id;
            this.stringField = stringField;
            this.moneyField = moneyField;
        }
    }

    public static class TestEntityProjectionNoConstructorWithParameter {
        Long id;
        String stringField;
        Money moneyField;

        public TestEntityProjectionNoConstructorWithParameter() {

        }
    }

    public static class TestEntityProjectionWithSelectPath {
        Long id;
        String stringField;
        String childStringField;

        public TestEntityProjectionWithSelectPath(
                Long id,
                @SelectPath("stringField") String stringField,
                @SelectPath("childEntity.stringField") String childStringField) {

            this.id = id;
            this.stringField = stringField;
            this.childStringField = childStringField;
        }
    }

}