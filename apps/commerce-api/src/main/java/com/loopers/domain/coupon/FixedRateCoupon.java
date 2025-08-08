package com.loopers.domain.coupon;

import com.loopers.domain.product.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("FIXED_RATE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FixedRateCoupon extends Coupon {

    @Embedded
    private DiscountRate discountRate;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "max_discount_amount"))
    private Money maxDiscountAmount;

    private FixedRateCoupon(
        String name,
        String userId,
        DiscountRate discountRate,
        Money maxDiscountAmount,
        Money minOrderAmount,
        LocalDateTime expiredAt
    ) {
        super(name, userId, minOrderAmount, expiredAt);
        this.discountRate = discountRate;
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public static FixedRateCoupon create(
        String name,
        String userId,
        DiscountRate discountRate,
        Money maxDiscountAmount,
        Money minOrderAmount,
        LocalDateTime expiredAt
    ) {
        return new FixedRateCoupon(
            name,
            userId,
            discountRate,
            maxDiscountAmount,
            minOrderAmount,
            expiredAt
        );
    }

    @Override
    public Money calculateDiscountAmount(Money orderAmount) {
        BigDecimal discountAmount = orderAmount.getValue()
            .multiply(discountRate.getRate())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);

        if (maxDiscountAmount != null &&
            discountAmount.compareTo(maxDiscountAmount.getValue()) > 0
        ) {
            return maxDiscountAmount;
        }

        return Money.of(discountAmount);
    }

    @Override
    public CouponType getType() {
        return CouponType.FIXED_RATE;
    }
}
