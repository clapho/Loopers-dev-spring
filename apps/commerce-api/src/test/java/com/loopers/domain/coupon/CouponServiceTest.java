package com.loopers.domain.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.domain.product.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @DisplayName("쿠폰 적용")
    @Nested
    class Apply {

        @DisplayName("정액 할인 쿠폰을 정상적으로 적용한다.")
        @Test
        void apply_fixedAmount() {
            // given
            Long couponId = 1L;
            String userId = "user1";
            Money orderAmount = Money.of(50000);

            FixedAmountCoupon coupon = FixedAmountCoupon.create(
                "5000원 할인 쿠폰",
                userId,
                DiscountAmount.of(BigDecimal.valueOf(5000)),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );

            when(couponRepository.findByIdAndUserId(couponId, userId))
                .thenReturn(Optional.of(coupon));
            when(couponRepository.save(any(Coupon.class)))
                .thenReturn(coupon);

            // when
            Money discountAmount = couponService.apply(couponId, userId, orderAmount);

            // then
            assertThat(discountAmount).isEqualTo(Money.of(5000));
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.USED);
            assertThat(coupon.getUsedAt()).isNotNull();

            verify(couponRepository).findByIdAndUserId(couponId, userId);
            verify(couponRepository).save(coupon);
        }

        @DisplayName("정률 할인 쿠폰을 정상적으로 적용한다.")
        @Test
        void apply_fixedRate() {
            // given
            Long couponId = 1L;
            String userId = "user1";
            Money orderAmount = Money.of(50000);

            FixedRateCoupon coupon = FixedRateCoupon.create(
                "10% 할인 쿠폰",
                userId,
                DiscountRate.of(BigDecimal.valueOf(10.0)),
                Money.of(10000),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );

            when(couponRepository.findByIdAndUserId(couponId, userId))
                .thenReturn(Optional.of(coupon));
            when(couponRepository.save(any(Coupon.class)))
                .thenReturn(coupon);

            // when
            Money discountAmount = couponService.apply(couponId, userId, orderAmount);

            // then
            assertThat(discountAmount).isEqualTo(Money.of(5000));
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.USED);
            assertThat(coupon.getUsedAt()).isNotNull();

            verify(couponRepository).findByIdAndUserId(couponId, userId);
            verify(couponRepository).save(coupon);
        }

        @DisplayName("정률 할인 쿠폰의 최대 할인 금액이 적용된다.")
        @Test
        void apply_fixedRate_withMaxLimit() {
            // given
            Long couponId = 1L;
            String userId = "user1";
            Money orderAmount = Money.of(100000);

            FixedRateCoupon coupon = FixedRateCoupon.create(
                "10% 할인 쿠폰", userId,
                DiscountRate.of(BigDecimal.valueOf(10.0)),
                Money.of(5000),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );

            when(couponRepository.findByIdAndUserId(couponId, userId))
                .thenReturn(Optional.of(coupon));
            when(couponRepository.save(any(Coupon.class)))
                .thenReturn(coupon);

            // when
            Money discountAmount = couponService.apply(couponId, userId, orderAmount);

            // then
            assertThat(discountAmount).isEqualTo(Money.of(5000));
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.USED);

            verify(couponRepository).findByIdAndUserId(couponId, userId);
            verify(couponRepository).save(coupon);
        }

        @DisplayName("존재하지 않는 쿠폰으로 요청시 예외가 발생한다.")
        @Test
        void fail_whenCouponNotExists() {
            // given
            Long nonExistentCouponId = 999L;
            String userId = "user1";
            Money orderAmount = Money.of(50000);

            when(couponRepository.findByIdAndUserId(nonExistentCouponId, userId))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.apply(nonExistentCouponId, userId, orderAmount))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("사용자의 쿠폰을 찾을 수 없습니다.");
                });

            verify(couponRepository).findByIdAndUserId(nonExistentCouponId, userId);
        }

        @DisplayName("이미 사용된 쿠폰으로 요청시 예외가 발생한다.")
        @Test
        void fail_whenCouponAlreadyUsed() {
            // given
            Long couponId = 1L;
            String userId = "user1";
            Money orderAmount = Money.of(50000);

            FixedAmountCoupon coupon = FixedAmountCoupon.create(
                "5000원 할인 쿠폰",
                userId,
                DiscountAmount.of(BigDecimal.valueOf(5000)),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );
            coupon.use(orderAmount);

            when(couponRepository.findByIdAndUserId(couponId, userId))
                .thenReturn(Optional.of(coupon));

            // when & then
            assertThatThrownBy(() -> couponService.apply(couponId, userId, orderAmount))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("이미 사용된 쿠폰입니다.");
                });

            verify(couponRepository).findByIdAndUserId(couponId, userId);
        }

        @DisplayName("최소 주문 금액 미달시 예외가 발생한다.")
        @Test
        void fail_whenOrderAmountBelowMinimum() {
            // given
            Long couponId = 1L;
            String userId = "user1";
            Money orderAmount = Money.of(20000);

            FixedAmountCoupon coupon = FixedAmountCoupon.create(
                "5000원 할인 쿠폰",
                userId,
                DiscountAmount.of(BigDecimal.valueOf(5000)),
                Money.of(30000),
                LocalDateTime.now().plusDays(7)
            );

            when(couponRepository.findByIdAndUserId(couponId, userId))
                .thenReturn(Optional.of(coupon));

            // when & then
            assertThatThrownBy(() -> couponService.apply(couponId, userId, orderAmount))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).contains("최소 주문 금액");
                    assertThat(coreException.getMessage()).contains("30000");
                });

            verify(couponRepository).findByIdAndUserId(couponId, userId);
        }
    }
}
