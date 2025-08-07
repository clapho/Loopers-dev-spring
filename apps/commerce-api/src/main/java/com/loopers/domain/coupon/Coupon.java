package com.loopers.domain.coupon;

import com.loopers.domain.product.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupon")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "coupon_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "min_order_amount"))
    private Money minOrderAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private LocalDateTime usedAt;

    protected Coupon(
        String name,
        String userId,
        Money minOrderAmount,
        LocalDateTime expiredAt
    ) {
        if (name == null || name.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "쿠폰명은 필수입니다."
            );
        }

        if (userId == null || userId.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "User ID는 필수입니다."
            );
        }

        if (expiredAt == null || expiredAt.isBefore(LocalDateTime.now())) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "만료일은 현재 시간 이후여야 합니다."
            );
        }

        this.name = name;
        this.userId = userId;
        this.status = CouponStatus.ACTIVE;
        this.minOrderAmount = minOrderAmount;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = expiredAt;
    }

    public abstract Money calculateDiscountAmount(Money orderAmount);

    public abstract CouponType getType();

    public void use(Money orderAmount) {
        if (status == CouponStatus.USED) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "이미 사용된 쿠폰입니다."
            );
        }

        if (status != CouponStatus.ACTIVE) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "사용할 수 없는 쿠폰입니다."
            );
        }

        if (LocalDateTime.now().isAfter(expiredAt)) {
            this.status = CouponStatus.EXPIRED;
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "만료된 쿠폰입니다."
            );
        }

        if (orderAmount.getValue().compareTo(minOrderAmount.getValue()) < 0) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                String.format(
                    "최소 주문 금액 %s원 이상이어야 쿠폰을 사용할 수 있습니다.",
                    minOrderAmount.getValue()
                )
            );
        }

        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
