package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DiscountAmount {

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal amount;

    private DiscountAmount(BigDecimal amount) {
        if (amount == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "할인 금액은 필수입니다."
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "할인 금액은 0 이상이어야 합니다."
            );
        }

        this.amount = amount;
    }

    public static DiscountAmount of(BigDecimal amount) {
        return new DiscountAmount(amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DiscountAmount that = (DiscountAmount) obj;
        return amount.compareTo(that.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.hashCode();
    }
}
