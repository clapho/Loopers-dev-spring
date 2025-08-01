package com.loopers.application.order;

import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.Quantity;

public class OrderItemInfo {

    public static record Detail(
        Long productId,
        Money price,
        Quantity quantity,
        Money totalPrice
    ) {
        public static Detail from(OrderItem orderItem) {
            return new Detail(
                orderItem.getProductId(),
                orderItem.getPrice(),
                orderItem.getQuantity(),
                orderItem.getTotalPrice()
            );
        }
    }
}
