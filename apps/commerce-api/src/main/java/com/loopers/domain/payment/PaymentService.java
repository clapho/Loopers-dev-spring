package com.loopers.domain.payment;

import com.loopers.domain.product.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment createCardPayment(Long orderId, String userId, Money amount,
        String cardType, String cardNo) {
        Payment payment = Payment.createForCard(orderId, userId, amount, cardType, cardNo);
        return paymentRepository.save(payment);
    }

    public Payment createPointPayment(Long orderId, String userId, Money amount) {
        Payment payment = Payment.createForPoint(orderId, userId, amount);
        return paymentRepository.save(payment);
    }

    public Payment startProcessing(Long paymentId, String transactionKey) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "결제 정보를 찾을 수 없습니다."
            ));
        payment.startProcessing(transactionKey);
        return paymentRepository.save(payment);
    }

    public Payment completeSuccess(String transactionKey) {
        Payment payment = paymentRepository.findByTransactionKey(transactionKey)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "거래키에 해당하는 결제 정보를 찾을 수 없습니다."
            ));
        payment.completeSuccess();
        return paymentRepository.save(payment);
    }

    public Payment completeFailure(String transactionKey, String reason) {
        Payment payment = paymentRepository.findByTransactionKey(transactionKey)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "거래키에 해당하는 결제 정보를 찾을 수 없습니다."
            ));
        payment.completeFailure(reason);
        return paymentRepository.save(payment);
    }

    public Payment cancel(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "결제 정보를 찾을 수 없습니다."
            ));
        payment.cancel();
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "결제 정보를 찾을 수 없습니다."
            ));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByTransactionKey(String transactionKey) {
        return paymentRepository.findByTransactionKey(transactionKey)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "거래키에 해당하는 결제 정보를 찾을 수 없습니다."
            ));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrderAndUser(Long orderId, String userId) {
        return paymentRepository.findByOrderIdAndUserId(orderId, userId);
    }

    @Transactional(readOnly = true)
    public Payment getSuccessfulPaymentByOrder(Long orderId) {
        return paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.SUCCESS)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "해당 주문의 성공한 결제 정보를 찾을 수 없습니다."
            ));
    }
}
