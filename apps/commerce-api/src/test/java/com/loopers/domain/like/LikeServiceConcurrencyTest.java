package com.loopers.domain.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.Quantity;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
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
class LikeConcurrencyTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동일한 상품에 대해 여러명이 동시에 좋아요를 요청해도, 상품의 좋아요 개수가 정상 반영되어야 한다")
    @Test
    void concurrentLikeFromMultipleUsers_correctCount() throws InterruptedException {
        // given
        Brand brand = Brand.create("브랜드", "브랜드 설명");
        brandRepository.save(brand);

        Product product = Product.create(
            "상품1",
            Money.of(BigDecimal.valueOf(10000)),
            Quantity.of(100),
            brand.getId()
        );
        Product savedProduct = productRepository.save(product);

        int userCount = 10;
        for (int i = 1; i <= userCount; i++) {
            User user = new User(
                "user" + i,
                "사용자" + i,
                Gender.M,
                "user" + i + "@gmail.com",
                "1995-03-01"
            );
            userRepository.save(user);
        }

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 1; i <= threadCount; i++) {
            final String userId = "user" + i;
            executorService.submit(() -> {
                try {
                    likeService.like(userId, savedProduct.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("좋아요 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        long likeCount = likeService.countByProduct(savedProduct.getId());

        assertAll(
            () -> assertThat(likeCount).isEqualTo(userCount),
            () -> assertThat(successCount.get()).isEqualTo(userCount)
        );
    }

    @DisplayName("동일한 상품에 대해 여러명이 좋아요와 좋아요 취소를 동시에 요청해도, 최종 좋아요 개수가 정확해야 한다")
    @Test
    void concurrentLikeAndUnlike_correctFinalCount() throws InterruptedException {
        // given
        Brand brand = Brand.create("브랜드", "브랜드 설명");
        brandRepository.save(brand);

        Product product = Product.create(
            "테스트 상품",
            Money.of(BigDecimal.valueOf(10000)),
            Quantity.of(100),
            brand.getId()
        );
        Product savedProduct = productRepository.save(product);

        // 20명의 사용자 (10명은 좋아요, 10명은 좋아요 취소)
        int totalUsers = 20;
        for (int i = 1; i <= totalUsers; i++) {
            User user = new User(
                "user" + i,
                "사용자" + i,
                Gender.M,
                "user" + i + "@gmail.com",
                "1995-03-01"
            );
            userRepository.save(user);

            if (i > 10) {
                likeService.like("user" + i, savedProduct.getId());
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(totalUsers);
        CountDownLatch latch = new CountDownLatch(totalUsers);

        // when
        for (int i = 1; i <= totalUsers; i++) {
            final String userId = "user" + i;
            final boolean isLikeOperation = i <= 10; // 1-10: 좋아요, 11-20: 취소

            executorService.submit(() -> {
                try {
                    if (isLikeOperation) {
                        likeService.like(userId, savedProduct.getId());
                    } else {
                        likeService.unlike(userId, savedProduct.getId());
                    }
                } catch (Exception e) {
                    System.out.println("좋아요/취소 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        long finalLikeCount = likeService.countByProduct(savedProduct.getId());

        assertThat(finalLikeCount).isEqualTo(10);
    }
}
