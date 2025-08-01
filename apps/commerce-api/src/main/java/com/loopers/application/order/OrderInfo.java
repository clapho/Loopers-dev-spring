package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.product.Money;
import java.time.LocalDateTime;
import java.util.List;

public class OrderInfo {

    public static record Detail(
        Long orderId,
        String userId,
        Money totalPrice,
        OrderStatus status,
        List<OrderItemInfo.Detail> items,
        LocalDateTime orderedAt
    ) {
        public static Detail from(Order order) {
            List<OrderItemInfo.Detail> itemInfos = order.getItems().stream()
                .map(OrderItemInfo.Detail::from)
                .toList();

            return new Detail(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getStatus(),
                itemInfos,
                order.getOrderedAt()
            );
        }
    }

    public static record Summary(
        Long orderId,
        Money totalPrice,
        OrderStatus status,
        int itemCount,
        LocalDateTime orderedAt
    ) {
        public static Summary from(Order order) {
            return new Summary(
                order.getId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getItems().size(),
                order.getOrderedAt()
            );
        }
    }

    public static record OrderList(
        List<Summary> orders
    ) {
        public static OrderList of(List<Summary> orders) {
            return new OrderList(orders);
        }
    }
}
