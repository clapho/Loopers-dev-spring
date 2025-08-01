package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Table(name = "product")
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    private Money price;

    @Embedded
    private Quantity stockQuantity;

    private Long brandId;

    private LocalDateTime createdAt;

    protected Product() {}

    private Product(String name, Money price, Quantity stockQuantity, Long brandId) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.brandId = brandId;
        this.createdAt = LocalDateTime.now();
    }

    public static Product create(
        String name,
        Money price,
        Quantity stockQuantity,
        Long brandId
    ) {
        if (name == null || name.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "상품명은 필수입니다."
            );
        }

        if (price == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "가격은 필수입니다."
            );
        }

        if (stockQuantity == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "재고 수량은 필수입니다."
            );
        }

        if (brandId == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "브랜드 ID는 필수입니다."
            );
        }


        return new Product(name, price, stockQuantity, brandId);
    }

    public void decreaseStock(Quantity quantity) {
        if (this.stockQuantity.getValue() < quantity.getValue()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "재고 수량이 부족합니다."
            );
        }

        this.stockQuantity = this.stockQuantity.subtract(quantity);
    }
}
