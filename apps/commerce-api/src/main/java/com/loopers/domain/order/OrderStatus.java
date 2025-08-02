package com.loopers.domain.order;

public enum OrderStatus {
    PENDING("주문 대기"),
    COMPLETED("주문 완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
