package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
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
class ProductConcurrencyTest {

    @Autowired
    private ProductService productService;

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

    @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감되어야 한다")
    @Test
    void concurrentStockDecrease() throws InterruptedException {
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

        int threadCount = 10;
        int decreaseAmountPerThread = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStock(savedProduct.getId(), Quantity.of(decreaseAmountPerThread));
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    failCount.incrementAndGet();
                    System.out.println("재고 차감 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Product finalProduct = productService.findById(savedProduct.getId());
        int expectedStock = 100 - (successCount.get() * decreaseAmountPerThread);

        assertAll(
            () -> assertThat(finalProduct.getStockQuantity().getValue()).isEqualTo(expectedStock),
            () -> assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount),
            () -> assertThat(successCount.get()).isEqualTo(10),
            () -> assertThat(failCount.get()).isEqualTo(0)
        );
    }

    @DisplayName("재고가 부족한 상황에서 동시 차감 시 일관성이 유지되어야 한다")
    @Test
    void concurrentStockDecrease_whenInsufficientStock() throws InterruptedException {
        // given
        Brand brand = Brand.create("브랜드", "브랜드 설명");
        brandRepository.save(brand);

        Product product = Product.create(
            "상품1",
            Money.of(BigDecimal.valueOf(10000)),
            Quantity.of(30),
            brand.getId()
        );
        Product savedProduct = productRepository.save(product);

        int threadCount = 10;
        int decreaseAmountPerThread = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStock(savedProduct.getId(), Quantity.of(decreaseAmountPerThread));
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
        Product finalProduct = productService.findById(savedProduct.getId());

        assertAll(
            () -> assertThat(successCount.get()).isEqualTo(6),
            () -> assertThat(failCount.get()).isEqualTo(4),
            () -> assertThat(finalProduct.getStockQuantity().getValue()).isEqualTo(0),
            () -> assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount)
        );
    }
}
