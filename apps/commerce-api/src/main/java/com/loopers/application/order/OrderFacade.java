package com.loopers.application.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
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
    private final CouponService couponService;

    @Transactional
    public OrderInfo.Detail createOrder(OrderCommand.Create command) {
        validateUserExists(command.userId());

        Order order = Order.create(command.userId());

        for (OrderItemCommand.Create itemRequest : command.items()) {
            Product product = productService.get(itemRequest.productId());

            product.decreaseStock(itemRequest.quantity());

            order.addOrderItem(
                product.getId(),
                product.getPrice(),
                itemRequest.quantity()
            );
        }

        if (command.couponId() != null) {
            Coupon coupon = couponService.get(command.couponId(), command.userId());
            order.applyCoupon(coupon);
        }

        order.startPayment();
        Order savedOrder = orderService.place(order);

        log.info("주문 생성 완료. orderId: {}, userId: {}, finalPrice: {}",
            savedOrder.getId(), command.userId(), savedOrder.getFinalPrice());

        return OrderInfo.Detail.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderInfo.Detail getOrderDetail(OrderCommand.GetDetail command) {
        validateUserExists(command.userId());

        Order order = orderService.get(command.orderId(), command.userId());
        return OrderInfo.Detail.from(order);
    }

    @Transactional(readOnly = true)
    public OrderInfo.OrderList getMyOrders(OrderCommand.GetMyOrders command) {
        validateUserExists(command.userId());

        List<Order> orders = orderService.getAllByUser(command.userId());

        List<OrderInfo.Summary> orderSummaries = orders.stream()
            .map(OrderInfo.Summary::from)
            .toList();

        return OrderInfo.OrderList.of(orderSummaries);
    }

    private void validateUserExists(String userId) {
        userService.get(userId);
    }
}
