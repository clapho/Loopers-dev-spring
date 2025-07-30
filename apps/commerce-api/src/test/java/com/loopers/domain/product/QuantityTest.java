package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QuantityTest {

    @DisplayName("Quantity 생성")
    @Nested
    class Create {
        @Test
        @DisplayName("유효한 수량으로 Quantity 를 생성한다")
        void create() {
            //when
            Quantity quantity = Quantity.of(10);

            //then
            assertThat(quantity.getValue()).isEqualTo(10);
        }

        @Test
        @DisplayName("0으로 Quantity 를 생성한다")
        void createWithZero() {
            //when
            Quantity quantity = Quantity.of(0);

            //then
            assertThat(quantity.getValue()).isEqualTo(0);
        }

        @ParameterizedTest
        @DisplayName("음수로 Quantity 를 생성하면 예외가 발생한다")
        @ValueSource(ints = {-1, -10, -100})
        void fail_whenValueIsNegative(int negativeValue) {
            //when & then
            assertThatThrownBy(() -> Quantity.of(negativeValue))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("수량은 0 이상이어야 합니다");
        }
    }

    @DisplayName("Quantity 차감")
    @Nested
    class Subtract {
        @Test
        @DisplayName("Quantity 를 차감한다")
        void subtract() {
            //given
            Quantity quantity1 = Quantity.of(10);
            Quantity quantity2 = Quantity.of(3);

            //when
            Quantity result = quantity1.subtract(quantity2);

            //then
            assertThat(result.getValue()).isEqualTo(7);
            assertThat(result).isEqualTo(Quantity.of(7));
        }

        @Test
        @DisplayName("부족한 Quantity 를 차감하려 하면 예외가 발생한다")
        void fail_whenInsufficientQuantity() {
            //given
            Quantity smallQuantity = Quantity.of(5);
            Quantity largeQuantity = Quantity.of(10);

            //when & then
            assertThatThrownBy(() -> smallQuantity.subtract(largeQuantity))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("수량이 부족합니다");
                });
        }
    }
}
