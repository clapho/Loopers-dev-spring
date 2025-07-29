package com.loopers.domain.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class BrandTest {

    @DisplayName("브랜드를 생성한다.")
    @Test
    void create() {
        String name = "Nike";
        String description = "Nike 입니다.";

        Brand brand = Brand.create(name, description);

        assertThat(brand.getId()).isNull();
        assertThat(brand.getName()).isEqualTo(name);
        assertThat(brand.getDescription()).isEqualTo(description);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("브랜드명이 유효하지 않으면 예외가 발생한다")
    void fail_whenBrandNameIsInvalid(String invalidName) {
        assertThatThrownBy(() -> Brand.create(invalidName, "Nike 입니다."))
            .isInstanceOf(CoreException.class)
            .satisfies(exception -> {
                CoreException coreException = (CoreException) exception;
                assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                assertThat(coreException.getMessage()).isEqualTo("브랜드명은 필수입니다.");
            });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("브랜드 설명이 유효하지 않으면 예외가 발생한다")
    void fail_whenDescriptionIsInvalid(String invalidDescription) {
        assertThatThrownBy(() -> Brand.create("Nike", invalidDescription))
            .isInstanceOf(CoreException.class)
            .satisfies(exception -> {
                CoreException coreException = (CoreException) exception;
                assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                assertThat(coreException.getMessage()).isEqualTo("브랜드 설명은 필수입니다.");
            });
    }
}
