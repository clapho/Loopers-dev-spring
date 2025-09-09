package com.loopers.domain.order;

public enum OrderStatus {
    PENDING("주문 대기"),
    PAYMENT_PENDING("결제 대기"),
    PAYMENT_PROCESSING("결제 처리중"),
    PAYMENT_FAILED("결제 실패"),
    COMPLETED("주문 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPaymentRequired() {
        return this == PENDING || this == PAYMENT_PENDING;
    }

    public boolean isCompleted() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean canCancel() {
        return this != COMPLETED && this != CANCELLED;
    }
}
