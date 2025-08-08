package com.loopers.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.DiscountAmount;
import com.loopers.domain.coupon.FixedAmountCoupon;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.order.external.ExternalOrderService;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.Quantity;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@DisplayName("OrderFacade 통합테스트")
public class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private PointService pointService;

    @Autowired
    private CouponRepository couponRepository;

    @MockitoSpyBean
    private OrderService orderService;

    @MockitoSpyBean
    private OrderRepository orderRepository;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @MockitoSpyBean
    private BrandRepository brandRepository;

    @MockitoSpyBean
    private UserRepository userRepository;

    @MockitoSpyBean
    private PointRepository pointRepository;

    @MockitoSpyBean
    private ExternalOrderService externalOrderService;

    @MockitoSpyBean
    private CouponService couponService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 생성")
    @Nested
    class CreateOrder {

        @Test
        @DisplayName("주문 생성이 성공한다")
        void createOrder() {
            // given
            String userId = "user1";
            userService.signUp(userId, "사용자1", Gender.M, "abc@gmail.com", "1995-03-01");

            pointService.chargePoint(userId, 50000L);

            Brand brand1 = brandService.create("brand1", "description1");
            Product product1 = productService.create("product1", Money.of(10000L), Quantity.of(100), brand1.getId());
            Product product2 = productService.create("product2", Money.of(20000L), Quantity.of(50), brand1.getId());

            List<OrderItemCommand.Create> items = List.of(
                new OrderItemCommand.Create(product1.getId(), Quantity.of(2)),
                new OrderItemCommand.Create(product2.getId(), Quantity.of(1))
            );

            OrderCommand.Create command = new OrderCommand.Create(userId, items, null);

            // when
            OrderInfo.Detail result = orderFacade.createOrder(command);

            // then
            verify(userRepository, times(1)).findByUserId(userId);
            verify(orderRepository, times(2)).save(any());
            verify(externalOrderService, times(1)).sendOrderToExternalSystem(any());

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.userId()).isEqualTo(userId),
                () -> assertThat(result.totalPrice()).isEqualTo(Money.of(40000L)),
                () -> assertThat(result.status()).isEqualTo(OrderStatus.COMPLETED),
                () -> assertThat(result.items()).hasSize(2)
            );

            OrderItemInfo.Detail firstItem = result.items().get(0);
            OrderItemInfo.Detail secondItem = result.items().get(1);

            assertAll(
                () -> assertThat(firstItem.productId()).isEqualTo(product1.getId()),
                () -> assertThat(firstItem.quantity()).isEqualTo(Quantity.of(2)),
                () -> assertThat(firstItem.totalPrice()).isEqualTo(Money.of(20000L)),

                () -> assertThat(secondItem.productId()).isEqualTo(product2.getId()),
                () -> assertThat(secondItem.quantity()).isEqualTo(Quantity.of(1)),
                () -> assertThat(secondItem.totalPrice()).isEqualTo(Money.of(20000L))
            );
        }

        @Test
        @DisplayName("쿠폰 적용하여 주문 생성이 성공한다")
        void createOrder_withCoupon() {
            // given
            String userId = "user1";
            userService.signUp(userId, "사용자1", Gender.M, "abc@gmail.com", "1995-03-01");
            pointService.chargePoint(userId, 50000L);

            Brand brand1 = brandService.create("brand1", "description1");
            Product product1 = productService.create("product1", Money.of(10000L), Quantity.of(100), brand1.getId());

            FixedAmountCoupon coupon = FixedAmountCoupon.create(
                "1000원 할인 쿠폰",
                userId,
                DiscountAmount.of(BigDecimal.valueOf(1000)),
                Money.of(5000),
                LocalDateTime.now().plusDays(7)
            );
            Coupon savedCoupon = couponRepository.save(coupon);

            List<OrderItemCommand.Create> items = List.of(
                new OrderItemCommand.Create(product1.getId(), Quantity.of(1))
            );

            OrderCommand.Create command = new OrderCommand.Create(userId, items, savedCoupon.getId());

            // when
            OrderInfo.Detail result = orderFacade.createOrder(command);

            // then
            verify(userRepository, times(1)).findByUserId(userId);
            verify(couponService, times(1)).getUserCoupon(savedCoupon.getId(), userId);
            verify(orderRepository, times(2)).save(any());

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.userId()).isEqualTo(userId),
                () -> assertThat(result.totalPrice()).isEqualTo(Money.of(10000L)),
                () -> assertThat(result.status()).isEqualTo(OrderStatus.COMPLETED),
                () -> assertThat(result.items()).hasSize(1)
            );
        }

        @Test
        @DisplayName("재고가 부족할 때 주문 생성이 실패한다")
        void fail_whenStockInsufficient() {
            // given
            String userId = "user1";
            userService.signUp(userId, "사용자1", Gender.M, "abc@gmail.com", "1995-03-01");
            pointService.chargePoint(userId, 50000L);

            Brand brand1 = brandService.create("brand1", "description1");
            Product product1 = productService.create("product1", Money.of(10000L), Quantity.of(1), brand1.getId());

            List<OrderItemCommand.Create> items = List.of(
                new OrderItemCommand.Create(product1.getId(), Quantity.of(2))
            );

            OrderCommand.Create command = new OrderCommand.Create(userId, items, null);

            // when & then
            assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("재고 수량이 부족합니다.");
                });

            verify(userRepository, times(1)).findByUserId(userId);
            verify(productRepository, times(1)).findById(product1.getId());
        }

        @Test
        @DisplayName("포인트가 부족할 때 주문 생성이 실패한다")
        void fail_whenPointInsufficient() {
            // given
            String userId = "user1";
            userService.signUp(userId, "사용자1", Gender.M, "abc@gmail.com", "1995-03-01");
            pointService.chargePoint(userId, 5000L);

            Brand brand1 = brandService.create("brand1", "description1");
            Product product1 = productService.create("product1", Money.of(10000L), Quantity.of(100), brand1.getId());

            List<OrderItemCommand.Create> items = List.of(
                new OrderItemCommand.Create(product1.getId(), Quantity.of(1))
            );

            OrderCommand.Create command = new OrderCommand.Create(userId, items, null);

            // when & then
            assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).contains("포인트가 부족합니다.");
                });
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 주문 생성 시 실패한다")
        void fail_whenUserNotExists() {
            // given
            String nonExistentUserId = "nonexistent";
            Brand brand1 = brandService.create("brand1", "description1");
            Product product1 = productService.create("product1", Money.of(10000L), Quantity.of(100), brand1.getId());

            List<OrderItemCommand.Create> items = List.of(
                new OrderItemCommand.Create(product1.getId(), Quantity.of(1))
            );

            OrderCommand.Create command = new OrderCommand.Create(nonExistentUserId, items, null);

            // when & then
            assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("사용자가 존재하지 않습니다.");
                });

            verify(userRepository, times(1)).findByUserId(nonExistentUserId);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 주문 생성 시 실패한다")
        void fail_whenProductNotExists() {
            // given
            String userId = "user1";
            userService.signUp(userId, "사용자1", Gender.M, "abc@gmail.com", "1995-03-01");
            pointService.chargePoint(userId, 50000L);

            Long nonExistentProductId = 999L;
            List<OrderItemCommand.Create> items = List.of(
                new OrderItemCommand.Create(nonExistentProductId, Quantity.of(1))
            );

            OrderCommand.Create command = new OrderCommand.Create(userId, items, null);

            // when & then
            assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("상품이 존재하지 않습니다.");
                });

            verify(userRepository, times(1)).findByUserId(userId);
            verify(productRepository, times(1)).findById(nonExistentProductId);
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰으로 주문 생성 시 실패한다")
        void fail_whenCouponNotExists() {
            // given
            String userId = "user1";
            userService.signUp(userId, "사용자1", Gender.M, "abc@gmail.com", "1995-03-01");
            pointService.chargePoint(userId, 50000L);

            Brand brand1 = brandService.create("brand1", "description1");
            Product product1 = productService.create("product1", Money.of(10000L), Quantity.of(100), brand1.getId());

            List<OrderItemCommand.Create> items = List.of(
                new OrderItemCommand.Create(product1.getId(), Quantity.of(1))
            );

            Long nonExistentCouponId = 999L;
            OrderCommand.Create command = new OrderCommand.Create(userId, items, nonExistentCouponId);

            // when & then
            assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).contains("쿠폰을 찾을 수 없습니다");
                });

            verify(userRepository, times(1)).findByUserId(userId);
            verify(couponService, times(1)).getUserCoupon(nonExistentCouponId, userId);
        }
    }

    @DisplayName("주문 상세 조회")
    @Nested
    class GetOrderDetail {

        @Test
        @DisplayName("주문 상세 조회가 성공한다")
        void getOrderDetail() {
            // given
            String userId = "user1";
            userService.signUp(userId, "사용자1", Gender.M, "abc@gmail.com", "1995-03-01");
            pointService.chargePoint(userId, 50000L);

            Brand brand1 = brandService.create("brand1", "description1");
            Product product1 = productService.create("product1", Money.of(10000L), Quantity.of(100), brand1.getId());

            List<OrderItemCommand.Create> items = List.of(
                new OrderItemCommand.Create(product1.getId(), Quantity.of(2))
            );

            OrderCommand.Create createCommand = new OrderCommand.Create(userId, items, null);
            OrderInfo.Detail createdOrder = orderFacade.createOrder(createCommand);

            OrderCommand.GetDetail command = new OrderCommand.GetDetail(createdOrder.orderId(), userId);

            // when
            OrderInfo.Detail result = orderFacade.getOrderDetail(command);

            // then
            verify(userRepository, times(2)).findByUserId(userId); // 생성 1번, 조회 1번
            verify(orderRepository, times(1)).findByIdAndUserId(createdOrder.orderId(), userId);

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.orderId()).isEqualTo(createdOrder.orderId()),
                () -> assertThat(result.userId()).isEqualTo(userId),
                () -> assertThat(result.totalPrice()).isEqualTo(Money.of(20000L)),
                () -> assertThat(result.status()).isEqualTo(OrderStatus.COMPLETED),
                () -> assertThat(result.items()).hasSize(1)
            );
        }
    }

    @DisplayName("내 주문 목록 조회")
    @Nested
    class GetMyOrders {

        @Test
        @DisplayName("내 주문 목록 조회가 성공한다")
        void getMyOrders() {
            // given
            String userId = "user1";
            userService.signUp(userId, "사용자1", Gender.M, "abc@gmail.com", "1995-03-01");
            pointService.chargePoint(userId, 100000L);

            Brand brand1 = brandService.create("brand1", "description1");
            Product product1 = productService.create("product1", Money.of(10000L), Quantity.of(100), brand1.getId());
            Product product2 = productService.create("product2", Money.of(20000L), Quantity.of(100), brand1.getId());

            // 2개 주문 생성
            OrderCommand.Create createCommand1 = new OrderCommand.Create(userId,
                List.of(new OrderItemCommand.Create(product1.getId(), Quantity.of(1))), null);
            OrderCommand.Create createCommand2 = new OrderCommand.Create(userId,
                List.of(new OrderItemCommand.Create(product2.getId(), Quantity.of(2))), null);

            orderFacade.createOrder(createCommand1);
            orderFacade.createOrder(createCommand2);

            OrderCommand.GetMyOrders command = new OrderCommand.GetMyOrders(userId);

            // when
            OrderInfo.OrderList result = orderFacade.getMyOrders(command);

            // then
            verify(userRepository, times(3)).findByUserId(userId); // 생성 2번, 조회 1번
            verify(orderRepository, times(1)).findByUserIdOrderByOrderedAtDesc(userId);

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.orders()).hasSize(2),
                () -> assertThat(result.orders().get(0).totalPrice()).isEqualTo(Money.of(40000L)),
                () -> assertThat(result.orders().get(1).totalPrice()).isEqualTo(Money.of(10000L))
            );
        }

        @Test
        @DisplayName("주문이 없을 때 빈 목록을 반환한다")
        void getMyOrders_whenNoOrders() {
            // given
            String userId = "user1";
            userService.signUp(userId, "사용자1", Gender.M, "abc@gmail.com", "1995-03-01");

            OrderCommand.GetMyOrders command = new OrderCommand.GetMyOrders(userId);

            // when
            OrderInfo.OrderList result = orderFacade.getMyOrders(command);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.orders()).isEmpty()
            );
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 주문 목록 조회 시 실패한다")
        void fail_whenUserNotExists() {
            // given
            String nonExistentUserId = "nonexistent";
            OrderCommand.GetMyOrders command = new OrderCommand.GetMyOrders(nonExistentUserId);

            // when & then
            assertThatThrownBy(() -> orderFacade.getMyOrders(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("사용자가 존재하지 않습니다.");
                });

            verify(userRepository, times(1)).findByUserId(nonExistentUserId);
        }
    }
}
