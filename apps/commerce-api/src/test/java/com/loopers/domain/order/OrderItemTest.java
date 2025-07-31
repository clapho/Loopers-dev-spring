package com.loopers.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.domain.product.Money;
import com.loopers.domain.product.Quantity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OrderItemTest {

    @DisplayName("주문 아이템 생성")
    @Nested
    class Create {

        @DisplayName("주문 아이템을 생성한다.")
        @Test
        void create() {
            //given
            Long productId = 1L;
            Money price = Money.of(10000L);
            Quantity quantity = Quantity.of(3);

            //when
            OrderItem orderItem = OrderItem.create(productId, price, quantity);

            //then
            assertThat(orderItem.getId()).isNull();
            assertThat(orderItem.getProductId()).isEqualTo(productId);
            assertThat(orderItem.getPrice()).isEqualTo(price);
            assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        }

        @DisplayName("상품 ID가 null 이면 예외를 반환한다.")
        @Test
        void fail_whenProductIdIsNull() {
            //given
            Money price = Money.of(10000L);
            Quantity quantity = Quantity.of(1);

            // when & then
            assertThatThrownBy(() -> OrderItem.create(null, price, quantity))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("Product ID는 필수입니다.");
                });
        }

        @DisplayName("가격이 null 이면 예외를 반환한다.")
        @Test
        void fail_whenPriceIsNull() {
            //given
            Long productId = 1L;
            Quantity quantity = Quantity.of(1);

            // when & then
            assertThatThrownBy(() -> OrderItem.create(productId, null, quantity))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("가격은 필수입니다.");
                });
        }

        @DisplayName("수량이 null 이면 예외를 반환한다.")
        @Test
        void fail_whenQuantityIsNull() {
            //given
            Long productId = 1L;
            Money price = Money.of(10000L);

            // when & then
            assertThatThrownBy(() -> OrderItem.create(productId, price, null))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("수량은 필수입니다.");
                });
        }
    }

    @DisplayName("총 가격 계산")
    @Nested
    class GetTotalPrice {

        @ParameterizedTest
        @DisplayName("가격과 수량으로 총 가격을 계산한다.")
        @ValueSource(ints = {1, 2, 5, 10})
        void getTotalPrice(int quantity) {
            //given
            Long productId = 1L;
            Money price = Money.of(5000L);
            OrderItem orderItem = OrderItem.create(productId, price, Quantity.of(quantity));

            //when
            Money totalPrice = orderItem.getTotalPrice();

            //then
            assertThat(totalPrice).isEqualTo(Money.of(5000L * quantity));
        }

        @DisplayName("수량이 0인 경우에도 총 가격을 계산한다.")
        @Test
        void getTotalPrice_whenQuantityIsZero() {
            //given
            Long productId = 1L;
            Money price = Money.of(10000L);
            OrderItem orderItem = OrderItem.create(productId, price, Quantity.of(0));

            //when
            Money totalPrice = orderItem.getTotalPrice();

            //then
            assertThat(totalPrice).isEqualTo(Money.of(0L));
        }
    }
}
