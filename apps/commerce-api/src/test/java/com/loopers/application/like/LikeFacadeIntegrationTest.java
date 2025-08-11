package com.loopers.application.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
@DisplayName("LikeFacade 통합테스트")
public class LikeFacadeIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @MockitoSpyBean
    private LikeService likeService;

    @MockitoSpyBean
    private LikeRepository likeRepository;

    @MockitoSpyBean
    private UserRepository userRepository;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @MockitoSpyBean
    private BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요 등록")
    @Nested
    class Add {

        @Test
        @DisplayName("좋아요 등록이 성공한다")
        void addLike() {
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

            LikeCommand.Add command = new LikeCommand.Add(userId, product.getId());

            // when
            LikeInfo.Like result = likeFacade.addLike(command);

            // then
            verify(userRepository, times(1)).findByUserId(userId);
            verify(productRepository, times(1)).findById(product.getId());
            verify(likeService, times(1)).isLiked(userId, product.getId());
            verify(likeService, times(1)).like(userId, product.getId());
            verify(likeService, times(1)).countByProduct(product.getId());

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.productId()).isEqualTo(product.getId()),
                () -> assertThat(result.isLiked()).isTrue(),
                () -> assertThat(result.likeCount()).isEqualTo(1)
            );
        }

        @Test
        @DisplayName("이미 좋아요한 상품에 대해 멱등성이 보장된다")
        void addLike_idempotent_whenAlreadyLiked() {
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

            LikeCommand.Add command = new LikeCommand.Add(userId, product.getId());
            likeFacade.addLike(command);

            // when
            LikeInfo.Like result = likeFacade.addLike(command);

            // then
            verify(likeService, times(2)).isLiked(userId, product.getId());
            verify(likeService, times(1)).like(userId, product.getId());
            verify(likeService, times(2)).countByProduct(product.getId());

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.productId()).isEqualTo(product.getId()),
                () -> assertThat(result.isLiked()).isTrue(),
                () -> assertThat(result.likeCount()).isEqualTo(1)
            );
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 좋아요 등록 시 실패한다")
        void fail_whenUserNotExists() {
            // given
            Brand brand = brandService.create("brand1", "description1");

            Product product = productService.create(
                "product1",
                Money.of(10000L),
                Quantity.of(100),
                brand.getId()
            );

            String nonExistentUserId = "nonexistent";
            LikeCommand.Add command = new LikeCommand.Add(nonExistentUserId, product.getId());

            // when & then
            assertThatThrownBy(() -> likeFacade.addLike(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("사용자가 존재하지 않습니다.");
                });

            verify(userRepository, times(1)).findByUserId(nonExistentUserId);
            verify(likeService, never()).like(anyString(), anyLong());
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 좋아요 등록 시 실패한다")
        void fail_whenProductNotExists() {
            // given
            String userId = "user1";

            userService.signUp(
                userId,
                "사용자1",
                Gender.M,
                "abc@gmail.com",
                "1995-03-01"
            );

            Long nonExistentProductId = 999L;
            LikeCommand.Add command = new LikeCommand.Add(userId, nonExistentProductId);

            // when & then
            assertThatThrownBy(() -> likeFacade.addLike(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("상품이 존재하지 않습니다.");
                });

            verify(userRepository, times(1)).findByUserId(userId);
            verify(productRepository, times(1)).findById(nonExistentProductId);
            verify(likeService, never()).like(anyString(), anyLong());
        }
    }

    @DisplayName("좋아요 취소")
    @Nested
    class Remove {

        @Test
        @DisplayName("기존 좋아요 취소가 성공한다")
        void removeLike() {
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

            LikeCommand.Add addCommand = new LikeCommand.Add(userId, product.getId());
            likeFacade.addLike(addCommand);

            LikeCommand.Remove removeCommand = new LikeCommand.Remove(userId, product.getId());

            // when
            LikeInfo.Like result = likeFacade.removeLike(removeCommand);

            // then
            verify(likeService, times(1)).unlike(userId, product.getId());

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.productId()).isEqualTo(product.getId()),
                () -> assertThat(result.isLiked()).isFalse(),
                () -> assertThat(result.likeCount()).isEqualTo(0)
            );
        }

        @Test
        @DisplayName("좋아요하지 않은 상품에 대해 멱등성이 보장된다")
        void removeLike_idempotent_whenNotLiked() {
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

            LikeCommand.Remove command = new LikeCommand.Remove(userId, product.getId());

            // when
            LikeInfo.Like result = likeFacade.removeLike(command);

            // then
            verify(likeService, times(1)).isLiked(userId, product.getId());
            verify(likeService, never()).unlike(userId, product.getId());
            verify(likeService, times(1)).countByProduct(product.getId());

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.productId()).isEqualTo(product.getId()),
                () -> assertThat(result.isLiked()).isFalse(),
                () -> assertThat(result.likeCount()).isEqualTo(0)
            );
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 좋아요 취소 시 실패한다")
        void fail_whenUserNotExists() {
            // given
            Brand brand = brandService.create("brand1", "description1");

            Product product = productService.create(
                "product1",
                Money.of(10000L),
                Quantity.of(100),
                brand.getId()
            );

            String nonExistentUserId = "nonexistent";
            LikeCommand.Remove command = new LikeCommand.Remove(nonExistentUserId, product.getId());

            // when & then
            assertThatThrownBy(() -> likeFacade.removeLike(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("사용자가 존재하지 않습니다.");
                });

            verify(userRepository, times(1)).findByUserId(nonExistentUserId);
            verify(likeService, never()).unlike(anyString(), anyLong());
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 좋아요 취소 시 실패한다")
        void fail_whenProductNotExists() {
            // given
            String userId = "user1";

            userService.signUp(
                userId,
                "사용자1",
                Gender.M,
                "abc@gmail.com",
                "1995-03-01"
            );

            Long nonExistentProductId = 999L;
            LikeCommand.Remove command = new LikeCommand.Remove(userId, nonExistentProductId);

            // when & then
            assertThatThrownBy(() -> likeFacade.removeLike(command))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("상품이 존재하지 않습니다.");
                });

            verify(userRepository, times(1)).findByUserId(userId);
            verify(productRepository, times(1)).findById(nonExistentProductId);
            verify(likeService, never()).unlike(anyString(), anyLong());
        }
    }
}
