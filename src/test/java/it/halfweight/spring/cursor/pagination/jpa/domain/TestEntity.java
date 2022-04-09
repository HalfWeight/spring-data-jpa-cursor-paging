package it.halfweight.spring.cursor.pagination.jpa.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column
    public String stringField;

    @OneToOne(cascade = CascadeType.ALL)
    public TestChildEntity childEntity;

    @Embedded
    public Money moneyField;

    @Column
    public Timestamp date;

    public TestEntity() {
    }

    public TestEntity(String stringField, Money moneyField) {
        this.stringField = stringField;
        this.moneyField = moneyField;
    }

    public TestEntity(String stringField, Money moneyField, TestChildEntity childEntity) {
        this.stringField = stringField;
        this.moneyField = moneyField;
        this.childEntity = childEntity;
    }

    public TestEntity(String stringField, Instant date) {
        this.stringField = stringField;
        this.date = Timestamp.from(date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEntity that = (TestEntity) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("stringField", stringField)
                .add("childEntity", childEntity)
                .add("date", date)
                .toString();
    }

}
