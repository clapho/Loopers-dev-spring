package com.loopers.domain.order.external;

import com.loopers.domain.order.Order;

public interface ExternalOrderService {

    void sendOrderToExternalSystem(Order order);
}
