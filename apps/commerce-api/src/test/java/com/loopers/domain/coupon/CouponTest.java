package com.loopers.domain.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.product.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CouponTest {

    @DisplayName("정액 할인 쿠폰 생성")
    @Nested
    class CreateFixedAmount {

        @DisplayName("정액 할인 쿠폰을 생성한다.")
        @Test
        void create() {
            // given
            String name = "5000원 할인 쿠폰";
            String userId = "user1";
            DiscountAmount discountAmount = DiscountAmount.of(BigDecimal.valueOf(5000));
            Money minOrderAmount = Money.of(30000);
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

            // when
            FixedAmountCoupon coupon = FixedAmountCoupon.create(
                name,
                userId,
                discountAmount,
                minOrderAmount,
                expiredAt
            );

            // then
            assertAll(
                () -> assertThat(coupon.getId()).isNull(),
                () -> assertThat(coupon.getName()).isEqualTo(name),
                () -> assertThat(coupon.getUserId()).isEqualTo(userId),
                () -> assertThat(coupon.getDiscountAmount()).isEqualTo(discountAmount),
                () -> assertThat(coupon.getMinOrderAmount()).isEqualTo(minOrderAmount),
                () -> assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE),
                () -> assertThat(coupon.getType()).isEqualTo(CouponType.FIXED_AMOUNT),
                () -> assertThat(coupon.getExpiredAt()).isEqualTo(expiredAt),
                () -> assertThat(coupon.getUsedAt()).isNull()
            );
        }

        @DisplayName("쿠폰명이 유효하지 않은 경우 예외를 반환한다.")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        void fail_whenCouponNameIsInvalid(String invalidName) {
            // given
            String userId = "user1";
            DiscountAmount discountAmount = DiscountAmount.of(BigDecimal.valueOf(5000));
            Money minOrderAmount = Money.of(30000);
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

            // when & then
            assertThatThrownBy(() -> FixedAmountCoupon.create(
                invalidName, userId, discountAmount, minOrderAmount, expiredAt
            ))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("쿠폰명은 필수입니다.");
                });
        }

        @DisplayName("사용자 ID가 유효하지 않은 경우 예외를 반환한다.")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        void fail_whenUserIdIsInvalid(String invalidUserId) {
            // given
            String name = "5000원 할인 쿠폰";
            DiscountAmount discountAmount = DiscountAmount.of(BigDecimal.valueOf(5000));
            Money minOrderAmount = Money.of(30000);
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

            // when & then
            assertThatThrownBy(() -> FixedAmountCoupon.create(
                name, invalidUserId, discountAmount, minOrderAmount, expiredAt
            ))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("User ID는 필수입니다.");
                });
        }

        @DisplayName("만료일이 과거인 경우 예외를 반환한다.")
        @Test
        void fail_whenExpiredAtIsPast() {
            // given
            String name = "5000원 할인 쿠폰";
            String userId = "user1";
            DiscountAmount discountAmount = DiscountAmount.of(BigDecimal.valueOf(5000));
            Money minOrderAmount = Money.of(30000);
            LocalDateTime pastExpiredAt = LocalDateTime.now().minusDays(1);

            // when & then
            assertThatThrownBy(() -> FixedAmountCoupon.create(
                name, userId, discountAmount, minOrderAmount, pastExpiredAt
            ))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("만료일은 현재 시간 이후여야 합니다.");
                });
        }
    }

    @DisplayName("정률 할인 쿠폰 생성")
    @Nested
    class CreateFixedRate {

        @DisplayName("정률 할인 쿠폰을 생성한다.")
        @Test
        void create() {
            // given
            String name = "10% 할인 쿠폰";
            String userId = "user1";
            DiscountRate discountRate = DiscountRate.of(BigDecimal.valueOf(10.0));
            Money maxDiscountAmount = Money.of(5000);
            Money minOrderAmount = Money.of(30000);
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

            // when
            FixedRateCoupon coupon = FixedRateCoupon.create(
                name,
                userId,
                discountRate,
                maxDiscountAmount,
                minOrderAmount,
                expiredAt
            );

            // then
            assertAll(
                () -> assertThat(coupon.getId()).isNull(),
                () -> assertThat(coupon.getName()).isEqualTo(name),
                () -> assertThat(coupon.getUserId()).isEqualTo(userId),
                () -> assertThat(coupon.getDiscountRate()).isEqualTo(discountRate),
                () -> assertThat(coupon.getMaxDiscountAmount()).isEqualTo(maxDiscountAmount),
                () -> assertThat(coupon.getMinOrderAmount()).isEqualTo(minOrderAmount),
                () -> assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE),
                () -> assertThat(coupon.getType()).isEqualTo(CouponType.FIXED_RATE),
                () -> assertThat(coupon.getExpiredAt()).isEqualTo(expiredAt),
                () -> assertThat(coupon.getUsedAt()).isNull()
            );
        }
    }

    @DisplayName("할인 금액 계산")
    @Nested
    class CalculateDiscountAmount {

        @DisplayName("정액 할인 쿠폰의 할인 금액을 계산한다.")
        @Test
        void fixedAmount_calculateDiscount() {
            // given
            FixedAmountCoupon coupon = FixedAmountCoupon.create(
                "5000원 할인 쿠폰",
                "user1",
                DiscountAmount.of(BigDecimal.valueOf(5000)),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );
            Money orderAmount = Money.of(50000);

            // when
            Money discountAmount = coupon.calculateDiscountAmount(orderAmount);

            // then
            assertThat(discountAmount).isEqualTo(Money.of(5000));
        }

        @DisplayName("정률 할인 쿠폰의 할인 금액을 계산한다.")
        @Test
        void fixedRate_calculateDiscount() {
            // given
            FixedRateCoupon coupon = FixedRateCoupon.create(
                "10% 할인 쿠폰",
                "user1",
                DiscountRate.of(BigDecimal.valueOf(10.0)),
                Money.of(10000),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );
            Money orderAmount = Money.of(50000);

            // when
            Money discountAmount = coupon.calculateDiscountAmount(orderAmount);

            // then
            assertThat(discountAmount).isEqualTo(Money.of(5000));
        }

        @DisplayName("정률 할인 쿠폰의 최대 할인 금액을 초과하는 경우 최대 할인 금액을 반환한다.")
        @Test
        void fixedRate_calculateDiscount_withMaxLimit() {
            // given
            FixedRateCoupon coupon = FixedRateCoupon.create(
                "10% 할인 쿠폰",
                "user1",
                DiscountRate.of(BigDecimal.valueOf(10.0)),
                Money.of(3000),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );
            Money orderAmount = Money.of(50000);

            // when
            Money discountAmount = coupon.calculateDiscountAmount(orderAmount);

            // then
            assertThat(discountAmount).isEqualTo(Money.of(3000));
        }
    }

    @DisplayName("쿠폰 사용")
    @Nested
    class Use {

        @DisplayName("쿠폰을 정상적으로 사용한다.")
        @Test
        void use() {
            // given
            FixedAmountCoupon coupon = FixedAmountCoupon.create(
                "5000원 할인 쿠폰",
                "user1",
                DiscountAmount.of(BigDecimal.valueOf(5000)),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );
            Money orderAmount = Money.of(50000);

            // when
            coupon.use(orderAmount);

            // then
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.USED);
            assertThat(coupon.getUsedAt()).isNotNull();
        }

        @DisplayName("이미 사용된 쿠폰을 다시 사용하려 하면 예외가 발생한다.")
        @Test
        void fail_whenCouponAlreadyUsed() {
            // given
            FixedAmountCoupon coupon = FixedAmountCoupon.create(
                "5000원 할인 쿠폰",
                "user1",
                DiscountAmount.of(BigDecimal.valueOf(5000)),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );
            Money orderAmount = Money.of(50000);
            coupon.use(orderAmount);

            // when & then
            assertThatThrownBy(() -> coupon.use(orderAmount))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("이미 사용된 쿠폰입니다.");
                });
        }

        @DisplayName("최소 주문 금액 미달시 예외가 발생한다.")
        @Test
        void fail_whenOrderAmountBelowMinimum() {
            // given
            FixedAmountCoupon coupon = FixedAmountCoupon.create(
                "5000원 할인 쿠폰",
                "user1",
                DiscountAmount.of(BigDecimal.valueOf(5000)),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );
            Money orderAmount = Money.of(20000); // 최소 주문 금액 미달

            // when & then
            assertThatThrownBy(() -> coupon.use(orderAmount))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).contains("최소 주문 금액");
                    assertThat(coreException.getMessage()).contains("30000");
                });
        }
    }
}
