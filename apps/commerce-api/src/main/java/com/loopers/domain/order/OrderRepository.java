package com.loopers.domain.order;

import java.util.List;

public interface OrderRepository {

    Order save(Order order);

    Order findById(Long id);

    Order findByIdAndUserId(Long orderId, String userId);

    List<Order> findByUserIdOrderByOrderedAtDesc(String userId);
}
