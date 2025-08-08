package com.loopers.application.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.external.ExternalOrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacade {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;
    private final PointService pointService;
    private final ExternalOrderService externalOrderService;
    private final CouponService couponService;

    @Transactional
    public OrderInfo.Detail createOrder(OrderCommand.Create command) {
        validateUserExists(command.userId());

        Order order = Order.create(command.userId());

        for (OrderItemCommand.Create itemRequest : command.items()) {
            Product product = productService.findById(itemRequest.productId());

            product.decreaseStock(itemRequest.quantity());

            order.addOrderItem(
                product.getId(),
                product.getPrice(),
                itemRequest.quantity()
            );
        }

        if (command.couponId() != null) {
            Coupon coupon = couponService.getUserCoupon(command.couponId(), command.userId());
            order.applyCoupon(coupon);
        }

        pointService.usePoint(command.userId(), order.getFinalPrice().getValue().longValue());

        Order savedOrder = orderService.save(order);

        try {
            externalOrderService.sendOrderToExternalSystem(savedOrder);
        } catch (Exception e) {
            log.error("외부 시스템 전송 실패, 주문 ID: {}", savedOrder.getId(), e);
        }

        savedOrder.complete();
        Order completedOrder = orderService.save(savedOrder);

        return OrderInfo.Detail.from(completedOrder);
    }

    @Transactional(readOnly = true)
    public OrderInfo.Detail getOrderDetail(OrderCommand.GetDetail command) {
        validateUserExists(command.userId());

        Order order = orderService.findByIdAndUserId(command.orderId(), command.userId());

        return OrderInfo.Detail.from(order);
    }

    @Transactional(readOnly = true)
    public OrderInfo.OrderList getMyOrders(OrderCommand.GetMyOrders command) {
        validateUserExists(command.userId());

        List<Order> orders = orderService.findByUserId(command.userId());

        List<OrderInfo.Summary> orderSummaries = orders.stream()
            .map(OrderInfo.Summary::from)
            .toList();

        return OrderInfo.OrderList.of(orderSummaries);
    }

    private void validateUserExists(String userId) {
        userService.findByUserId(userId);
    }
}
