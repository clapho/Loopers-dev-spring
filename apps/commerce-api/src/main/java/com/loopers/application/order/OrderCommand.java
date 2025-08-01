package com.loopers.application.order;

import java.util.List;

public class OrderCommand {

    public static record Create(
        String userId,
        List<OrderItemCommand.Create> items
    ) {
    }

    public static record GetDetail(
        Long orderId,
        String userId
    ) {
    }

    public static record GetMyOrders(
        String userId
    ) {
    }
}
