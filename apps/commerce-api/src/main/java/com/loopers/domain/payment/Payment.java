package com.loopers.domain.payment;

import com.loopers.domain.product.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private String userId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "amount"))
    private Money amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionKey;

    private String paymentMethod;

    private String cardType;

    private String cardNo;

    private String failureReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Payment(
        Long orderId,
        String userId,
        Money amount,
        String paymentMethod
    ) {
        validatePaymentCreation(orderId, userId, amount, paymentMethod);

        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Payment createForCard(
        Long orderId,
        String userId,
        Money amount,
        String cardType,
        String cardNo
    ) {
        validateCardPayment(cardType, cardNo);

        Payment payment = new Payment(orderId, userId, amount, "CARD");
        payment.cardType = cardType;
        payment.cardNo = cardNo;
        return payment;
    }

    public static Payment createForPoint(
        Long orderId,
        String userId,
        Money amount
    ) {
        return new Payment(orderId, userId, amount, "POINT");
    }

    public void startProcessing(String transactionKey) {
        if (this.status != PaymentStatus.PENDING) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "결제 대기 상태에서만 처리를 시작할 수 있습니다."
            );
        }
        this.transactionKey = transactionKey;
        this.status = PaymentStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeSuccess() {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "처리중 상태에서만 성공 처리할 수 있습니다."
            );
        }
        this.status = PaymentStatus.SUCCESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeFailure(String reason) {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "처리중 상태에서만 실패 처리할 수 있습니다."
            );
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "완료된 결제는 취소할 수 없습니다."
            );
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCardPayment() {
        return "CARD".equals(this.paymentMethod);
    }

    public boolean isPointPayment() {
        return "POINT".equals(this.paymentMethod);
    }

    private static void validatePaymentCreation(
        Long orderId,
        String userId,
        Money amount,
        String paymentMethod
    ) {
        if (orderId == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "주문 ID는 필수입니다."
            );
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "사용자 ID는 필수입니다."
            );
        }
        if (amount == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "결제 금액은 필수입니다."
            );
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "결제 수단은 필수입니다."
            );
        }
    }

    private static void validateCardPayment(String cardType, String cardNo) {
        if (cardType == null || cardType.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "카드 타입은 필수입니다."
            );
        }
        if (cardNo == null || cardNo.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "카드 번호는 필수입니다."
            );
        }
    }
}
