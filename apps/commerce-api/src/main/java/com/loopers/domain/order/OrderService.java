package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public Order place(Order order) {
        return orderRepository.save(order);
    }

    public Order get(Long id) {
        Order order = orderRepository.findById(id);

        if (order == null) {
            throw new CoreException(
                ErrorType.NOT_FOUND,
                "주문이 존재하지 않습니다."
            );
        }

        return order;
    }

    public Order get(Long orderId, String userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId);

        if (order == null) {
            throw new CoreException(
                ErrorType.NOT_FOUND,
                "주문이 존재하지 않습니다."
            );
        }

        return order;
    }

    public List<Order> getAllByUser(String userId) {
        return orderRepository.findByUserIdOrderByOrderedAtDesc(userId);
    }
}
