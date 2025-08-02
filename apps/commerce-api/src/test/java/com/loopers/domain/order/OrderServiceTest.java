package com.loopers.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.domain.product.Money;
import com.loopers.domain.product.Quantity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @DisplayName("주문 저장")
    @Nested
    class Save {

        @DisplayName("주문을 저장한다.")
        @Test
        void save() {
            //given
            Order order = Order.create("user1");
            order.addOrderItem(1L, Money.of(10000L), Quantity.of(2));
            when(orderRepository.save(order)).thenReturn(order);

            //when
            Order result = orderService.save(order);

            //then
            assertThat(result).isEqualTo(order);
            verify(orderRepository).save(order);
        }
    }

    @DisplayName("주문 조회")
    @Nested
    class Find {

        @DisplayName("ID로 주문을 조회한다.")
        @Test
        void findById() {
            //given
            Long orderId = 1L;
            Order order = Order.create("user1");
            when(orderRepository.findById(orderId)).thenReturn(order);

            //when
            Order result = orderService.findById(orderId);

            //then
            assertThat(result).isEqualTo(order);
            verify(orderRepository).findById(orderId);
        }

        @DisplayName("존재하지 않는 주문 조회 시 예외가 발생한다")
        @Test
        void fail_whenOrderNotExists() {
            //given
            Long nonExistentId = 999L;
            when(orderRepository.findById(nonExistentId)).thenReturn(null);

            //when & then
            assertThatThrownBy(() -> orderService.findById(nonExistentId))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("주문이 존재하지 않습니다.");
                });

            verify(orderRepository).findById(nonExistentId);
        }

        @DisplayName("ID와 사용자 ID로 주문을 조회한다.")
        @Test
        void findByIdAndUserId() {
            //given
            Long orderId = 1L;
            String userId = "user1";
            Order order = Order.create(userId);
            when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(order);

            //when
            Order result = orderService.findByIdAndUserId(orderId, userId);

            //then
            assertThat(result).isEqualTo(order);
            assertThat(result.getUserId()).isEqualTo(userId);
            verify(orderRepository).findByIdAndUserId(orderId, userId);
        }

        @DisplayName("존재하지 않는 주문을 ID와 사용자 ID로 조회 시 예외가 발생한다")
        @Test
        void fail_whenOrderNotExistsByIdAndUserId() {
            //given
            Long orderId = 1L;
            String userId = "user1";
            when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(null);

            //when & then
            assertThatThrownBy(() -> orderService.findByIdAndUserId(orderId, userId))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("주문이 존재하지 않습니다.");
                });

            verify(orderRepository).findByIdAndUserId(orderId, userId);
        }

        @DisplayName("사용자 ID로 주문 목록을 조회한다.")
        @Test
        void findByUserId() {
            //given
            String userId = "user1";
            Order order1 = Order.create(userId);
            Order order2 = Order.create(userId);
            List<Order> orders = List.of(order1, order2);
            when(orderRepository.findByUserIdOrderByOrderedAtDesc(userId)).thenReturn(orders);

            //when
            List<Order> result = orderService.findByUserId(userId);

            //then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(order1, order2);
            verify(orderRepository).findByUserIdOrderByOrderedAtDesc(userId);
        }

        @DisplayName("사용자의 주문이 없으면 빈 목록을 반환한다.")
        @Test
        void findByUserId_whenNoOrders() {
            //given
            String userId = "testUser";
            when(orderRepository.findByUserIdOrderByOrderedAtDesc(userId)).thenReturn(List.of());

            //when
            List<Order> result = orderService.findByUserId(userId);

            //then
            assertThat(result).isEmpty();
            verify(orderRepository).findByUserIdOrderByOrderedAtDesc(userId);
        }
    }
}
