package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndUserId(Long orderId, String userId);

    List<Order> findByUserIdOrderByOrderedAtDesc(String userId);
}
