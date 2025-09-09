package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final PointService pointService;

    @Transactional
    public PaymentInfo.ProcessingResult processCardPayment(PaymentCommand.CreateCard command) {
        try {
            Payment payment = paymentService.createCardPayment(
                command.orderId(),
                command.userId(),
                command.amount(),
                command.cardType(),
                command.cardNo()
            );

            Order order = orderService.get(command.orderId(), command.userId());
            order.processPayment();
            orderService.place(order);

            String transactionKey = generateMockTransactionKey();
            paymentService.startProcessing(payment.getId(), transactionKey);

            simulateAsyncPaymentProcessing(transactionKey, command.orderId(), command.userId());

            log.info("카드 결제 요청 완료. orderId: {}, transactionKey: {}",
                command.orderId(), transactionKey);

            return PaymentInfo.ProcessingResult.success(transactionKey);

        } catch (Exception e) {
            log.error("카드 결제 요청 실패. orderId: {}", command.orderId(), e);
            return PaymentInfo.ProcessingResult.failed("결제 요청 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentInfo.ProcessingResult processPointPayment(PaymentCommand.CreatePoint command) {
        try {
            Payment payment = paymentService.createPointPayment(
                command.orderId(),
                command.userId(),
                command.amount()
            );

            Order order = orderService.get(command.orderId(), command.userId());
            order.processPayment();
            orderService.place(order);

            String transactionKey = "POINT_" + System.currentTimeMillis();
            paymentService.startProcessing(payment.getId(), transactionKey);

            pointService.use(command.userId(), command.amount().getValue().longValue());

            paymentService.completeSuccess(transactionKey);

            order.completePayment();
            orderService.place(order);

            log.info("포인트 결제 완료. orderId: {}", command.orderId());

            return PaymentInfo.ProcessingResult.success(transactionKey);

        } catch (Exception e) {
            log.error("포인트 결제 실패. orderId: {}", command.orderId(), e);
            return PaymentInfo.ProcessingResult.failed("포인트 결제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public void handlePaymentCallback(PaymentCommand.ProcessCallback command) {
        try {
            if (command.isSuccess()) {
                Payment payment = paymentService.completeSuccess(command.transactionKey());

                Order order = orderService.get(payment.getOrderId(), payment.getUserId());
                order.completePayment();
                orderService.place(order);

                log.info("결제 성공 콜백 처리 완료. transactionKey: {}, orderId: {}",
                    command.transactionKey(), payment.getOrderId());

            } else if (command.isFailed()) {
                Payment payment = paymentService.completeFailure(command.transactionKey(), command.reason());

                Order order = orderService.get(payment.getOrderId(), payment.getUserId());
                order.failPayment();
                orderService.place(order);

                log.warn("결제 실패 콜백 처리 완료. transactionKey: {}, orderId: {}, reason: {}",
                    command.transactionKey(), payment.getOrderId(), command.reason());
            }

        } catch (Exception e) {
            log.error("결제 콜백 처리 실패. transactionKey: {}", command.transactionKey(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PaymentInfo.Detail getPaymentDetail(PaymentCommand.GetDetail command) {
        Payment payment = paymentService.getPaymentByTransactionKey(command.transactionKey());
        return PaymentInfo.Detail.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentInfo.PaymentList getPaymentsByOrder(PaymentCommand.GetByOrder command) {
        return PaymentInfo.PaymentList.of(
            paymentService.getPaymentsByOrderAndUser(command.orderId(), command.userId())
        );
    }

    private void simulateAsyncPaymentProcessing(String transactionKey, Long orderId, String userId) {
        boolean isSuccess = Math.random() < 0.7;

        if (isSuccess) {
            handlePaymentCallback(new PaymentCommand.ProcessCallback(
                transactionKey, "SUCCESS", null
            ));
        } else {
            handlePaymentCallback(new PaymentCommand.ProcessCallback(
                transactionKey, "FAILED", "카드 한도 초과"
            ));
        }
    }

    private String generateMockTransactionKey() {
        return "TR_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
}
