package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.Quantity;
import java.util.List;

public class ProductInfo {

    public static record Detail(
        Long productId,
        String productName,
        Money price,
        Quantity stockQuantity,
        Long brandId,
        String brandName,
        String brandDescription,
        Long likeCount
    ) {
        public static Detail from(Product product, Brand brand, Long likeCount) {
            return new Detail(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                likeCount
            );
        }
    }

    public static record LikedProduct(
        Long productId,
        String productName,
        Money price,
        Quantity stockQuantity,
        Long brandId,
        String brandName
    ) {
        public static LikedProduct from(Product product, Brand brand) {
            return new LikedProduct(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                brand.getId(),
                brand.getName()
            );
        }
    }

    public static record PagedList(
        List<Detail> products,
        Integer currentPage,
        Integer totalPages,
        Long totalElements,
        Integer size,
        Boolean hasNext,
        Boolean hasPrevious
    ) {
        public static PagedList of(List<Detail> products, int page, int size, long total) {
            int totalPages = (int) Math.ceil((double) total / size);
            return new PagedList(
                products,
                page,
                totalPages,
                total,
                size,
                page < totalPages - 1,
                page > 0
            );
        }
    }
}
