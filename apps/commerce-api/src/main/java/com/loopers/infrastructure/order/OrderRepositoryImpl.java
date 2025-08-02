package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Order findById(Long id) {
        return orderJpaRepository.findById(id).orElse(null);
    }

    @Override
    public Order findByIdAndUserId(Long orderId, String userId) {
        return orderJpaRepository.findByIdAndUserId(orderId, userId).orElse(null);
    }

    @Override
    public List<Order> findByUserIdOrderByOrderedAtDesc(String userId) {
        return orderJpaRepository.findByUserIdOrderByOrderedAtDesc(userId);
    }
}
