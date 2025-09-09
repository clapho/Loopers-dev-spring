package com.loopers.application.payment;

import com.loopers.domain.product.Money;

public class PaymentCommand {

    public static record CreateCard(
        Long orderId,
        String userId,
        Money amount,
        String cardType,
        String cardNo,
        String callbackUrl
    ) {}

    public static record CreatePoint(
        Long orderId,
        String userId,
        Money amount
    ) {}

    public static record ProcessCallback(
        String transactionKey,
        String status,
        String reason
    ) {
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }

        public boolean isFailed() {
            return "FAILED".equals(status);
        }
    }

    public static record GetDetail(
        String transactionKey
    ) {}

    public static record GetByOrder(
        Long orderId,
        String userId
    ) {}
}
