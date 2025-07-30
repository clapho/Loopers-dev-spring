package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;


class ProductTest {

    @DisplayName("상품 생성")
    @Nested
    class Create {
        @DisplayName("상품을 생성한다.")
        @Test
        void create() {
            //given
            String name = "Cap";
            Money price = Money.of(10000L);
            Quantity stockQuantity = Quantity.of(10);
            Long brandId = 1L;

            //when
            Product product = Product.create(name, price, stockQuantity, brandId);

            //then
            assertThat(product.getId()).isNull();
            assertThat(product.getName()).isEqualTo(name);
            assertThat(product.getPrice()).isEqualTo(price);
            assertThat(product.getStockQuantity()).isEqualTo(stockQuantity);
            assertThat(product.getBrandId()).isEqualTo(brandId);
        }

        @DisplayName("상품명이 유효하지 않은 경우 예외를 반환한다.")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", " "})
        void fail_whenProductNameIsInvalid(String invalidName) {
            //given
            Money price = Money.of(10000L);
            Quantity stockQuantity = Quantity.of(10);
            Long brandId = 1L;

            // when & then
            assertThatThrownBy(() -> Product.create(invalidName, price, stockQuantity, brandId))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("상품명은 필수입니다.");
                });
        }

        @DisplayName("가격이 null 이면 예외를 반환한다.")
        @Test
        void fail_whenPriceIsNegative() {
            //given
            String name = "Cap";
            Quantity stockQuantity = Quantity.of(10);
            Long brandId = 1L;

            // when & then
            assertThatThrownBy(() -> Product.create(name, null, stockQuantity, brandId))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("가격은 필수입니다.");
                });
        }

        @DisplayName("재고 수량이 null 이면 예외를 반환한다.")
        @Test
        void fail_whenStockQuantityIsNull() {
            //given
            String name = "Cap";
            Money price = Money.of(10000L);
            Long brandId = 1L;

            // when & then
            assertThatThrownBy(() -> Product.create(name, price, null, brandId))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("재고 수량은 필수입니다.");
                });
        }

        @DisplayName("브랜드 ID가 null 이면 예외를 반환한다.")
        @Test
        void fail_whenBrandIdIsNull() {
            //given
            String name = "Cap";
            Money price = Money.of(10000L);
            Quantity stockQuantity = Quantity.of(10);
            Long brandId = 1L;

            // when & then
            assertThatThrownBy(() -> Product.create(name, price, stockQuantity, null))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("브랜드 ID는 필수입니다.");
                });
        }
    }

    @DisplayName("재고 차감")
    @Nested
    class DecreaseStock {

        @ParameterizedTest
        @DisplayName("재고를 정상적으로 차감한다.")
        @CsvSource({
            "10, 9, 1",   // 재고10, 차감1, 결과9
            "10, 5, 5",   // 재고10, 차감5, 결과5
            "10, 10, 0",  // 재고10, 차감10, 결과0 (경계값)
            "5, 0, 5"     // 재고5, 차감0, 결과5 (0차감)
        })
        void decreaseStock_success(int initialStock, int decreaseAmount, int expectedStock) {
            //given
            Product product = Product.create("상품", Money.of(10000L), Quantity.of(initialStock), 1L);

            //when
            product.decreaseStock(Quantity.of(decreaseAmount));

            //then
            assertThat(product.getStockQuantity()).isEqualTo(Quantity.of(expectedStock));
        }

        @Test
        @DisplayName("재고보다 많은 수량을 차감하려 하면 예외가 발생한다")
        void decreaseStock_fail_when_insufficient_stock() {
            //given
            Product product = Product.create("상품", Money.of(10000L), Quantity.of(5), 1L);

            //when & then
            assertThatThrownBy(() -> product.decreaseStock(Quantity.of(6)))
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("재고 수량이 부족합니다.");
                });
        }
    }
}
