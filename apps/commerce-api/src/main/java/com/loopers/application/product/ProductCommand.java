package com.loopers.application.product;

public class ProductCommand {

    public static record GetDetail(
        Long productId
    ) {

        public static GetDetail of(Long productId) {
            return new GetDetail(productId);
        }
    }

    public static record GetList(
        ProductSortOption sort, // 정렬 옵션
        Integer page,
        Integer size
    ) {
        public static GetList of(String sortCode, Integer page, Integer size) {
            return new GetList(
                ProductSortOption.fromCode(sortCode),
                page != null ? page : 0,
                size != null ? size : 20
            );
        }
    }

    public static record GetLikedProducts(
        String userId,
        Integer page,
        Integer size
    ) {
        public static GetLikedProducts of(String userId, Integer page, Integer size) {
            return new GetLikedProducts(
                userId,
                page != null ? page : 0,
                size != null ? size : 20
            );
        }
    }
}
