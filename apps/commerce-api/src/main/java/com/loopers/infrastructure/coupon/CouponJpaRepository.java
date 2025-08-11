package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByIdAndUserId(Long id, String userId);
}
