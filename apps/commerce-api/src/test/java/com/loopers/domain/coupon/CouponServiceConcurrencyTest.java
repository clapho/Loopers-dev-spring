package com.loopers.domain.coupon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.product.Money;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다")
    @Test
    void concurrentCouponUsage_onlyOneSucceeds() throws InterruptedException {
        // given
        String userId = "user1";
        FixedAmountCoupon coupon = FixedAmountCoupon.create(
            "1000원 할인 쿠폰",
            userId,
            DiscountAmount.of(BigDecimal.valueOf(1000)),
            Money.of(BigDecimal.valueOf(5000)),
            LocalDateTime.now().plusDays(7)
        );
        Coupon savedCoupon = couponRepository.save(coupon);

        Money orderAmount = Money.of(BigDecimal.valueOf(10000));
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    couponService.apply(savedCoupon.getId(), userId, orderAmount);
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    failCount.incrementAndGet();
                    System.out.println("쿠폰 사용 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Coupon finalCoupon = couponRepository.findByIdAndUserId(savedCoupon.getId(), userId)
            .orElseThrow();

        assertAll(
            () -> assertThat(successCount.get()).isEqualTo(1),
            () -> assertThat(failCount.get()).isGreaterThan(0),
            () -> assertThat(finalCoupon.getStatus()).isEqualTo(CouponStatus.USED),
            () -> assertThat(finalCoupon.getUsedAt()).isNotNull()
        );
    }}
