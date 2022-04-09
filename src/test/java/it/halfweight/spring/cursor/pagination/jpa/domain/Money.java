package it.halfweight.spring.cursor.pagination.jpa.domain;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@Embeddable
public class Money {

    public final static RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    public final static int DEFAULT_SCALE = 6;

    @Column(precision = 19, scale = 6, updatable = true)
    private BigDecimal value;

    @Column(updatable = true, length = 3)
    private Currency currency;

    protected Money() {

    }

    private Money(double value, Currency currency) {
        this.currency = currency;
        this.value = BigDecimal.valueOf(value).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static Money money(double value, Currency currency) {
        return new Money(value, currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equal(value, money.value) && Objects.equal(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, currency);
    }
}
