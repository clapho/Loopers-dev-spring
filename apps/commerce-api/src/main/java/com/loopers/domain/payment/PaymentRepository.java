package com.loopers.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByTransactionKey(String transactionKey);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByOrderIdAndUserId(Long orderId, String userId);

    List<Payment> findByUserIdAndStatus(String userId, PaymentStatus status);

    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
