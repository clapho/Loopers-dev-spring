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
public class DiscountRate {

    @Column(name = "discount_rate", precision = 19, scale = 2)
    private BigDecimal rate;

    private DiscountRate(BigDecimal rate) {
        if (rate == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "할인률은 필수입니다."
            );
        }

        if (rate.compareTo(BigDecimal.ZERO) < 0 ||
            rate.compareTo(BigDecimal.valueOf(100)) > 0
        ) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "할인률은 0에서 100 사이여야 합니다."
            );
        }

        this.rate = rate;
    }

    public static DiscountRate of(BigDecimal rate) {
        return new DiscountRate(rate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DiscountRate that = (DiscountRate) obj;
        return rate.compareTo(that.rate) == 0;
    }

    @Override
    public int hashCode() {
        return rate.hashCode();
    }
}
