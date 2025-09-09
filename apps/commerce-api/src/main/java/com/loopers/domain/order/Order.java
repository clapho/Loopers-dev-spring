package com.loopers.domain.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.Quantity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Entity
@Table(name = "orders")
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "total_price"))
    })
    private Money totalPrice;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "discount_amount"))
    private Money discountAmount;

    private String userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderedAt;

    protected Order() {}

    private Order(String userId) {
        this.userId = userId;
        this.totalPrice = Money.of(0L);
        this.discountAmount = Money.of(0L);
        this.status = OrderStatus.PENDING;
        this.orderedAt = LocalDateTime.now();
    }

    public static Order create(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "사용자 ID는 필수입니다."
            );
        }

        return new Order(userId);
    }

    public void addOrderItem(Long productId, Money productPrice, Quantity quantity) {
        OrderItem orderItem = OrderItem.create(productId, productPrice, quantity);
        this.items.add(orderItem);
        calculateTotalPrice();
    }

    public void applyCoupon(Coupon coupon) {
        if (coupon == null) {
            return;
        }

        coupon.use(this.totalPrice);

        Money discount = coupon.calculateDiscountAmount(this.totalPrice);

        this.discountAmount = discount;
    }

    public Money getFinalPrice() {
        return this.totalPrice.subtract(this.discountAmount);
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public void startPayment() {
        if (this.status != OrderStatus.PENDING) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "주문 대기 상태에서만 결제를 시작할 수 있습니다."
            );
        }
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void processPayment() {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "결제 대기 상태에서만 결제 처리를 시작할 수 있습니다."
            );
        }
        this.status = OrderStatus.PAYMENT_PROCESSING;
    }

    public void completePayment() {
        if (this.status != OrderStatus.PAYMENT_PROCESSING) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "결제 처리중 상태에서만 결제를 완료할 수 있습니다."
            );
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void failPayment() {
        if (this.status != OrderStatus.PAYMENT_PROCESSING) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "결제 처리중 상태에서만 결제 실패 처리할 수 있습니다."
            );
        }
        this.status = OrderStatus.PAYMENT_FAILED;
    }

    public void cancel() {
        if (!this.status.canCancel()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "취소할 수 없는 주문 상태입니다."
            );
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void calculateTotalPrice() {
        long total = items.stream()
            .mapToLong(item -> item.getTotalPrice().getValue().longValue())
            .sum();
        this.totalPrice = Money.of(total);
    }
}
