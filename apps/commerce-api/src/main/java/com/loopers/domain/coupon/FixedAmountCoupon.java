package com.loopers.domain.coupon;

import com.loopers.domain.product.Money;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("FIXED_AMOUNT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FixedAmountCoupon extends Coupon{

    @Embedded
    private DiscountAmount discountAmount;

    private FixedAmountCoupon(
        String name,
        String userId,
        DiscountAmount discountAmount,
        Money minOrderAmount,
        LocalDateTime expiredAt
    ) {
        super(name, userId, minOrderAmount, expiredAt);
        this.discountAmount = discountAmount;
    }

    public static FixedAmountCoupon create(
        String name,
        String userId,
        DiscountAmount discountAmount,
        Money minOrderAmount,
        LocalDateTime expiredAt
    ) {
        return new FixedAmountCoupon(
            name,
            userId,
            discountAmount,
            minOrderAmount,
            expiredAt
        );
    }

    @Override
    public Money calculateDiscountAmount(Money orderAmount) {
        return Money.of(discountAmount.getAmount());
    }

    @Override
    public CouponType getType() {
        return CouponType.FIXED_AMOUNT;
    }
}
