package com.loopers.application.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeService;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@DisplayName("ProductFacade 통합테스트")
public class ProductFacadeIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private LikeService likeService;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @MockitoSpyBean
    private BrandRepository brandRepository;

    @MockitoSpyBean
    private LikeRepository likeRepository;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 상세 조회")
    @Nested
    class GetProductDetail {

        @Test
        @DisplayName("상품 상세 조회가 성공한다")
        void getProductDetail() {
            // given
            Brand brand = brandService.create("brand1", "description1");

            Product product = productService.create(
                "product1",
                Money.of(10000L),
                Quantity.of(100),
                brand.getId()
            );

            String userId = "user1";
            userService.signUp(
                userId,
                "사용자1",
                Gender.M,
                "abc@gmail.com",
                "1995-03-01"
            );

            likeService.like(userId, product.getId());

            ProductCommand.GetDetail command = ProductCommand.GetDetail.of(product.getId());

            // when
            ProductInfo.Detail result = productFacade.getProductDetail(command);

            // then
            verify(productRepository, times(1)).findById(product.getId());
            verify(brandRepository, times(1)).findById(brand.getId());
            verify(likeRepository, times(1)).countByProductId(product.getId());

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.productId()).isEqualTo(product.getId()),
                () -> assertThat(result.productName()).isEqualTo("product1"),
                () -> assertThat(result.price()).isEqualTo(Money.of(10000L)),
                () -> assertThat(result.stockQuantity()).isEqualTo(Quantity.of(100)),
                () -> assertThat(result.brandId()).isEqualTo(brand.getId()),
                () -> assertThat(result.brandName()).isEqualTo("brand1"),
                () -> assertThat(result.brandDescription()).isEqualTo("description1"),
                () -> assertThat(result.likeCount()).isEqualTo(1L)
            );
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 상세 조회 시 실패한다")
        void fail_whenProductNotExists() {
            // given
            Long nonExistentProductId = 999L;
            ProductCommand.GetDetail command = ProductCommand.GetDetail.of(nonExistentProductId);

            // when & then
            assertThatThrownBy(() -> productFacade.getProductDetail(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("상품이 존재하지 않습니다.");
                });

            verify(productRepository, times(1)).findById(nonExistentProductId);
        }
    }

    @DisplayName("상품 목록 조회")
    @Nested
    class GetProducts {

        @Test
        @DisplayName("상품 목록 조회가 성공한다 (최신순)")
        void getProducts_latest() {
            // given
            Brand brand1 = brandService.create("brand1", "description1");
            Brand brand2 = brandService.create("brand2", "description2");

            Product product1 = productService.create(
                "product1",
                Money.of(10000L),
                Quantity.of(100),
                brand1.getId()
            );

            Product product2 = productService.create(
                "product2",
                Money.of(20000L),
                Quantity.of(50),
                brand2.getId()
            );

            ProductCommand.GetList command = ProductCommand.GetList.of("latest", 0, 10);

            // when
            ProductInfo.PagedList result = productFacade.getProducts(command);

            // then
            verify(productRepository, times(1)).countAll();

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.products()).hasSize(2),
                () -> assertThat(result.currentPage()).isEqualTo(0),
                () -> assertThat(result.size()).isEqualTo(10),
                () -> assertThat(result.totalElements()).isEqualTo(2L),
                () -> assertThat(result.hasNext()).isFalse(),
                () -> assertThat(result.hasPrevious()).isFalse(),
                () -> assertThat(result.products().get(0).productId()).isEqualTo(product2.getId()),
                () -> assertThat(result.products().get(1).productId()).isEqualTo(product1.getId())
            );
        }

        @Test
        @DisplayName("상품 목록 조회가 성공한다 (가격 낮은순)")
        void getProducts_priceAsc() {
            // given
            Brand brand1 = brandService.create("brand1", "description1");

            productService.create("product1", Money.of(20000L), Quantity.of(100), brand1.getId());
            productService.create("product2", Money.of(10000L), Quantity.of(50), brand1.getId());

            ProductCommand.GetList command = ProductCommand.GetList.of("price_asc", 0, 10);

            // when
            ProductInfo.PagedList result = productFacade.getProducts(command);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.products()).hasSize(2),
                () -> assertThat(result.products().get(0).price()).isEqualTo(Money.of(10000L)),
                () -> assertThat(result.products().get(1).price()).isEqualTo(Money.of(20000L))
            );
        }

        @Test
        @DisplayName("페이징이 올바르게 적용된다")
        void getProducts_withPaging() {
            // given
            Brand brand1 = brandService.create("brand1", "description1");

            // 5개 상품 생성
            for (int i = 1; i <= 5; i++) {
                productService.create(
                    "product" + i,
                    Money.of(10000L * i),
                    Quantity.of(100),
                    brand1.getId()
                );
            }

            ProductCommand.GetList command = ProductCommand.GetList.of("latest", 1, 2);

            // when
            ProductInfo.PagedList result = productFacade.getProducts(command);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.products()).hasSize(2),
                () -> assertThat(result.currentPage()).isEqualTo(1),
                () -> assertThat(result.size()).isEqualTo(2),
                () -> assertThat(result.totalElements()).isEqualTo(5L),
                () -> assertThat(result.totalPages()).isEqualTo(3),
                () -> assertThat(result.hasNext()).isTrue(),
                () -> assertThat(result.hasPrevious()).isTrue()
            );
        }
    }

    @DisplayName("좋아요한 상품 목록 조회")
    @Nested
    class GetLikedProducts {

        @Test
        @DisplayName("좋아요한 상품 목록 조회가 성공한다")
        void getLikedProducts() {
            // given
            Brand brand1 = brandService.create("brand1", "description1");
            Brand brand2 = brandService.create("brand2", "description2");

            Product product1 = productService.create(
                "product1",
                Money.of(10000L),
                Quantity.of(100),
                brand1.getId()
            );

            Product product2 = productService.create(
                "product2",
                Money.of(20000L),
                Quantity.of(50),
                brand2.getId()
            );

            Product product3 = productService.create(
                "product3",
                Money.of(15000L),
                Quantity.of(30),
                brand1.getId()
            );

            String userId = "user1";
            userService.signUp(
                userId,
                "사용자1",
                Gender.M,
                "abc@gmail.com",
                "1995-03-01"
            );

            likeService.like(userId, product1.getId());
            likeService.like(userId, product2.getId());

            ProductCommand.GetLikedProducts command = ProductCommand.GetLikedProducts.of(userId, 0, 10);

            // when
            ProductInfo.PagedList result = productFacade.getLikedProducts(command);

            // then
            verify(userRepository, times(1)).findByUserId(userId);
            verify(likeRepository, times(1)).countByUserId(userId);

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.products()).hasSize(2),
                () -> assertThat(result.currentPage()).isEqualTo(0),
                () -> assertThat(result.size()).isEqualTo(10),
                () -> assertThat(result.totalElements()).isEqualTo(2L),
                () -> assertThat(result.hasNext()).isFalse(),
                () -> assertThat(result.hasPrevious()).isFalse()
            );

            assertThat(result.products().stream()
                .map(ProductInfo.Detail::productId)
                .toList())
                .containsExactlyInAnyOrder(product1.getId(), product2.getId());
        }

        @Test
        @DisplayName("좋아요한 상품이 없을 때 빈 목록을 반환한다")
        void getLikedProducts_whenNoLikes() {
            // given
            String userId = "user1";
            userService.signUp(
                userId,
                "사용자1",
                Gender.M,
                "abc@gmail.com",
                "1995-03-01"
            );

            ProductCommand.GetLikedProducts command = ProductCommand.GetLikedProducts.of(userId, 0, 10);

            // when
            ProductInfo.PagedList result = productFacade.getLikedProducts(command);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.products()).isEmpty(),
                () -> assertThat(result.totalElements()).isEqualTo(0L)
            );
        }

        @Test
        @DisplayName("좋아요한 상품 목록 페이징이 올바르게 적용된다")
        void getLikedProducts_withPaging() {
            // given
            Brand brand1 = brandService.create("brand1", "description1");

            String userId = "user1";
            userService.signUp(
                userId,
                "사용자1",
                Gender.M,
                "abc@gmail.com",
                "1995-03-01"
            );

            // 5개 상품 생성하고 모두 좋아요
            for (int i = 1; i <= 5; i++) {
                Product product = productService.create(
                    "product" + i,
                    Money.of(10000L * i),
                    Quantity.of(100),
                    brand1.getId()
                );
                likeService.like(userId, product.getId());
            }

            ProductCommand.GetLikedProducts command = ProductCommand.GetLikedProducts.of(userId, 1, 2);

            // when
            ProductInfo.PagedList result = productFacade.getLikedProducts(command);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.products()).hasSize(2),
                () -> assertThat(result.currentPage()).isEqualTo(1),
                () -> assertThat(result.size()).isEqualTo(2),
                () -> assertThat(result.totalElements()).isEqualTo(5L),
                () -> assertThat(result.totalPages()).isEqualTo(3),
                () -> assertThat(result.hasNext()).isTrue(),
                () -> assertThat(result.hasPrevious()).isTrue()
            );
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 좋아요한 상품 목록 조회 시 실패한다")
        void fail_whenUserNotExists() {
            // given
            String nonExistentUserId = "nonexistent";
            ProductCommand.GetLikedProducts command = ProductCommand.GetLikedProducts.of(nonExistentUserId, 0, 10);

            // when & then
            assertThatThrownBy(() -> productFacade.getLikedProducts(command))
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
