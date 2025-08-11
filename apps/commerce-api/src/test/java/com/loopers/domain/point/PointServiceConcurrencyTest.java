package com.loopers.domain.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동일한 유저가 동시에 포인트를 사용해도 정상적으로 차감되어야 한다")
    @Test
    void concurrentPointUsage() throws InterruptedException {
        // given
        String userId = "user1";

        User user = new User(
            userId,
            "clap",
            Gender.M,
            "abc@gmail.com",
            "1995-03-01"
        );
        userRepository.save(user);

        Point point = new Point(userId);
        point.charge(1000L);
        pointRepository.save(point);

        int threadCount = 10;
        Long useAmountPerThread = 50L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.use(userId, useAmountPerThread);
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    failCount.incrementAndGet();
                    System.out.println("실패 사유: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Long finalAmount = pointService.getAmount(userId);
        Long expectedAmount = 1000L - (successCount.get() * useAmountPerThread);

        assertAll(
            () -> assertThat(finalAmount).isEqualTo(expectedAmount),
            () -> assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount)
        );
    }

    @DisplayName("포인트가 부족한 상황에서 동시 사용 시 일관성이 유지되어야 한다")
    @Test
    void concurrentPointUsage_whenInsufficientPoint() throws InterruptedException {
        // given
        String userId = "user1";

        User user = new User(
            userId,
            "clap",
            Gender.M,
            "abc@gmail.com",
            "1995-03-01"
        );
        userRepository.save(user);

        Point point = new Point(userId);
        point.charge(300L);
        pointRepository.save(point);

        int threadCount = 10;
        Long useAmountPerThread = 50L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.use(userId, useAmountPerThread);
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Long finalAmount = pointService.getAmount(userId);

        assertAll(
            () -> assertThat(successCount.get()).isEqualTo(6),
            () -> assertThat(failCount.get()).isEqualTo(4),
            () -> assertThat(finalAmount).isEqualTo(0L)
        );
    }
}
