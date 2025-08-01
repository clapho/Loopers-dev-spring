package com.loopers.application.product;

public enum ProductSortOption {
    LATEST("latest", "최신순"),
    PRICE_ASC("price_asc", "가격 낮은순"),
    LIKES_DESC("likes_desc", "좋아요 많은순");

    private final String code;
    private final String description;

    ProductSortOption(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ProductSortOption fromCode(String code) {
        if (code == null) {
            return LATEST;
        }

        for (ProductSortOption option : values()) {
            if (option.code.equals(code)) {
                return option;
            }
        }

        return LATEST;
    }
}
