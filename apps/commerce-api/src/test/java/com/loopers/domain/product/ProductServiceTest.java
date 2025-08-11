package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

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
            productService.create(name, price, stockQuantity, brandId);

            //then
            verify(productRepository).save(any(Product.class));
        }
    }

    @DisplayName("상품 조회")
    @Nested
    class Find {

        @DisplayName("ID로 상품을 조회한다.")
        @Test
        void findById() {
            //given
            Long productId = 1L;
            Product product = Product.create("Cap", Money.of(10000L), Quantity.of(10), 1L);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            //when
            Product result = productService.get(productId);

            //then
            assertThat(result).isEqualTo(product);
            verify(productRepository).findById(productId);
        }

        @DisplayName("존재하지 않는 상품 조회 시 예외가 발생한다")
        @Test
        void fail_whenProductNotExists() {
            //given
            Long nonExistentId = 999L;
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> productService.get(nonExistentId))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("상품이 존재하지 않습니다.");
                });

            verify(productRepository).findById(nonExistentId);
        }
    }
}
