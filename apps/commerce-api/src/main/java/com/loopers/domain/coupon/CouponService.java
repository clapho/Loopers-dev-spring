package com.loopers.domain.coupon;

import com.loopers.domain.product.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public Money apply(Long couponId, String userId, Money orderAmount) {
        try {
            Coupon coupon = get(couponId, userId);

            Money discountAmount = coupon.calculateDiscountAmount(orderAmount);

            coupon.use(orderAmount);
            couponRepository.save(coupon);

            return discountAmount;
        } catch (OptimisticLockingFailureException e) {
            throw new CoreException(
                ErrorType.CONFLICT,
                "쿠폰이 이미 사용되었습니다."
            );
        }
    }

    public Coupon get(Long couponId, String userId) {
        return couponRepository.findByIdAndUserId(couponId, userId)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "사용자의 쿠폰을 찾을 수 없습니다."
            ));
    }
}
