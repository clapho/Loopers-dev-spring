package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class Money {
    @Column(name = "money", precision = 19, scale = 2)
    private BigDecimal value;

    protected Money() {}

    private Money(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "금액은 0 이상이어야 합니다."
            );
        }

        this.value = value;
    }

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public static Money of(long value) {
        return new Money(BigDecimal.valueOf(value));
    }

    public Money subtract(Money other) {
        if (other == null) {
            return this;
        }

        BigDecimal result = this.value.subtract(other.value);
        return Money.of(result);
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Money money = (Money) obj;
        return value.compareTo(money.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
