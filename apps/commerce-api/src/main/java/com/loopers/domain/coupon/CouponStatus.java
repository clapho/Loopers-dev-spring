package com.loopers.domain.coupon;

import lombok.Getter;

@Getter
public enum CouponStatus {
    ACTIVE("활성"),
    USED("사용됨"),
    EXPIRED("만료됨");

    private final String description;

    CouponStatus(String description) {
        this.description = description;
    }
}
