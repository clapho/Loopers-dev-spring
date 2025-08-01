package com.loopers.application.order;

import com.loopers.domain.product.Quantity;

public class OrderItemCommand {

    public static record Create(
        Long productId,
        Quantity quantity
    ) {
    }
}
