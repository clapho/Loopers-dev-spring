package com.loopers.domain.order;

import com.loopers.domain.product.Money;
import com.loopers.domain.product.Quantity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "order_item")
@Getter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "stock_quantity"))
    })
    Quantity quantity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "price"))
    })
    Money price;

    Long productId;

    protected OrderItem() {}

    private OrderItem(Quantity quantity, Money price, Long productId) {
        this.quantity = quantity;
        this.price = price;
        this.productId = productId;
    }

    public static OrderItem create(Long productId, Money price, Quantity quantity) {
        if (productId == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "Product ID는 필수입니다."
            );
        }

        if (price == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "가격은 필수입니다."
            );
        }

        if (quantity == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "수량은 필수입니다."
            );
        }

        return new OrderItem(quantity, price, productId);
    }

    public Money getTotalPrice() {
        return Money.of(price.getValue().longValue() * quantity.getValue());
    }
}
