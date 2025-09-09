package com.loopers.domain.payment;

public enum PaymentStatus {
    PENDING("결제 대기"),
    PROCESSING("결제 처리중"),
    SUCCESS("결제 성공"),
    FAILED("결제 실패"),
    CANCELLED("결제 취소"),
    ;

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
