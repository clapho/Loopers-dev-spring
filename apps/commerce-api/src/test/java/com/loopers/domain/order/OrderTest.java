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
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class OrderTest {

    @DisplayName("주문 생성")
    @Nested
    class Create {

        @DisplayName("주문을 생성한다.")
        @Test
        void create() {
            //given
            String userId = "user1";

            //when
            Order order = Order.create(userId);

            //then
            assertThat(order.getId()).isNull();
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getTotalPrice()).isEqualTo(Money.of(0L));
            assertThat(order.getItems()).isEmpty();
            assertThat(order.getOrderedAt()).isNotNull();
        }

        @DisplayName("사용자 ID가 유효하지 않은 경우 예외를 반환한다.")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        void fail_whenUserIdIsInvalid(String invalidUserId) {
            // when & then
            assertThatThrownBy(() -> Order.create(invalidUserId))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("사용자 ID는 필수입니다.");
                });
        }
    }

    @DisplayName("주문 아이템 추가")
    @Nested
    class AddOrderItem {

        @DisplayName("주문 아이템을 추가한다.")
        @Test
        void addOrderItem() {
            //given
            Order order = Order.create("user1");
            Long productId = 1L;
            Money productPrice = Money.of(10000L);
            Quantity quantity = Quantity.of(2);

            //when
            order.addOrderItem(productId, productPrice, quantity);

            //then
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getTotalPrice()).isEqualTo(Money.of(20000L));

            OrderItem orderItem = order.getItems().get(0);
            assertThat(orderItem.getProductId()).isEqualTo(productId);
            assertThat(orderItem.getPrice()).isEqualTo(productPrice);
            assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        }

        @DisplayName("여러 주문 아이템을 추가하면 총 가격이 계산된다.")
        @Test
        void addMultipleOrderItems() {
            //given
            Order order = Order.create("user1");

            //when
            order.addOrderItem(1L, Money.of(10000L), Quantity.of(2));
            order.addOrderItem(2L, Money.of(15000L), Quantity.of(1));

            //then
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getTotalPrice()).isEqualTo(Money.of(35000L));
        }
    }

    @DisplayName("총 가격 계산")
    @Nested
    class CalculateTotalPrice {

        @DisplayName("주문 아이템들의 총 가격을 계산한다.")
        @Test
        void calculateTotalPrice() {
            //given
            Order order = Order.create("user1");
            order.addOrderItem(1L, Money.of(5000L), Quantity.of(3));
            order.addOrderItem(2L, Money.of(8000L), Quantity.of(2));
            order.addOrderItem(3L, Money.of(12000L), Quantity.of(1));

            //when & then
            assertThat(order.getTotalPrice()).isEqualTo(Money.of(43000L));
        }

        @DisplayName("주문 아이템이 없으면 총 가격은 0이다.")
        @Test
        void calculateTotalPrice_whenNoItems() {
            //given
            Order order = Order.create("user1");

            //when & then
            assertThat(order.getTotalPrice()).isEqualTo(Money.of(0L));
        }
    }
}
