package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentJpaRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByTransactionKey(String transactionKey) {
        return paymentJpaRepository.findByTransactionKey(transactionKey);
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<Payment> findByOrderIdAndUserId(Long orderId, String userId) {
        return paymentJpaRepository.findByOrderIdAndUserId(orderId, userId);
    }

    @Override
    public List<Payment> findByUserIdAndStatus(String userId, PaymentStatus status) {
        return paymentJpaRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status) {
        return paymentJpaRepository.findByOrderIdAndStatus(orderId, status);
    }
}
