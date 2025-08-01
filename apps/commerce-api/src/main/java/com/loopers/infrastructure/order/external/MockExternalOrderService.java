package com.loopers.infrastructure.order.external;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.external.ExternalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MockExternalOrderService implements ExternalOrderService {

    @Override
    public void sendOrderToExternalSystem(Order order) {
        log.info("외부 시스템으로 주문 정보 전송 - orderId: {}, userId: {}, amount: {}",
            order.getId(), order.getUserId(), order.getTotalPrice().getValue());

        log.info("외부 시스템 전송 완료");
    }
}
