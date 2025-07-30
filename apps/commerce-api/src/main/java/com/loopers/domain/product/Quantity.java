package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Quantity {
    @Column(name = "quantity")
    private int value;

    protected Quantity() {}

    private Quantity(int value) {
        if (value < 0) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "수량은 0 이상이어야 합니다."
            );
        }

        this.value = value;
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public Quantity subtract(Quantity other) {
        if (this.value < other.value) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "수량이 부족합니다"
            );
        }

        return new Quantity(this.value - other.value);
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Quantity quantity = (Quantity) obj;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
