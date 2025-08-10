package com.loopers.domain.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {
    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    @DisplayName("브랜드 생성")
    @Nested
    class Create {

        @DisplayName("브랜드를 생성한다.")
        @Test
        void create() {
            //given
            String name = "Nike";
            String description = "Nike 입니다.";

            Brand brand = Brand.create(name, description);

            when(brandRepository.save(any(Brand.class))).thenReturn(brand);

            //when
            Brand result = brandService.create(name, description);

            //then
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getDescription()).isEqualTo(description);

            verify(brandRepository).save(any(Brand.class));
        }

        @ParameterizedTest
        @DisplayName("브랜드명이 유효하지 않으면 예외가 발생한다")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ",})
        void fail_whenBrandNameIsInvalid(String invalidName) {
            //when & then
            assertThatThrownBy(() -> brandService.create(invalidName, "설명"))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("브랜드명은 필수입니다.");
                });

            verify(brandRepository, never()).save(any(Brand.class));
        }

        @ParameterizedTest
        @DisplayName("설명이 유효하지 않으면 예외가 발생한다")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void fail_whenDescriptionIsInvalid(String invalidDescription) {
            //when & then
            assertThatThrownBy(() -> brandService.create("Nike", invalidDescription))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("브랜드 설명은 필수입니다.");
                });

            verify(brandRepository, never()).save(any(Brand.class));
        }
    }

    @DisplayName("브랜드 조회")
    @Nested
    class Find {

        @Test
        @DisplayName("ID로 브랜드를 조회한다")
        void findById() {
            //given
            Long brandId = 1L;
            Brand brand = Brand.create("Nike", "Nike 입니다.");

            when(brandRepository.findById(brandId)).thenReturn(brand);

            //when
            Brand result = brandService.get(brandId);

            //then
            assertThat(result).isEqualTo(brand);
            verify(brandRepository).findById(brandId);
        }

        @Test
        @DisplayName("존재하지 않는 브랜드 조회 시 예외가 발생한다")
        void fail_whenBrandNotExists() {
            //given
            Long nonExistentId = 999L;
            when(brandRepository.findById(nonExistentId)).thenReturn(null);

            //when & then
            assertThatThrownBy(() -> brandService.get(nonExistentId))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo("브랜드가 존재하지 않습니다.");
                });

            verify(brandRepository).findById(nonExistentId);
        }
    }
}
