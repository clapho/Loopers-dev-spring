package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionKey(String transactionKey);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByOrderIdAndUserId(Long orderId, String userId);

    List<Payment> findByUserIdAndStatus(String userId, PaymentStatus status);

    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId ORDER BY p.createdAt DESC")
    List<Payment> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);
}
