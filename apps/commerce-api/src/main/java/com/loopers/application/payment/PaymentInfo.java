package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.product.Money;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentInfo {

    public static record Detail(
        Long id,
        Long orderId,
        String userId,
        Money amount,
        PaymentStatus status,
        String transactionKey,
        String paymentMethod,
        String cardType,
        String cardNo,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        public static Detail from(Payment payment) {
            return new Detail(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionKey(),
                payment.getPaymentMethod(),
                payment.getCardType(),
                payment.getCardNo(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
            );
        }

        public Money getFinalPrice() {
            return amount;
        }
    }

    public static record Summary(
        Long id,
        Long orderId,
        Money amount,
        PaymentStatus status,
        String paymentMethod,
        LocalDateTime createdAt
    ) {
        public static Summary from(Payment payment) {
            return new Summary(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getCreatedAt()
            );
        }
    }

    public static record PaymentList(
        List<Summary> payments
    ) {
        public static PaymentList of(List<Payment> payments) {
            List<Summary> summaries = payments.stream()
                .map(Summary::from)
                .toList();
            return new PaymentList(summaries);
        }
    }

    public static record ProcessingResult(
        String transactionKey,
        PaymentStatus status,
        String message
    ) {
        public static ProcessingResult success(String transactionKey) {
            return new ProcessingResult(
                transactionKey,
                PaymentStatus.PROCESSING,
                "결제 요청이 정상적으로 처리되었습니다."
            );
        }

        public static ProcessingResult failed(String message) {
            return new ProcessingResult(
                null,
                PaymentStatus.FAILED,
                message
            );
        }
    }
}
