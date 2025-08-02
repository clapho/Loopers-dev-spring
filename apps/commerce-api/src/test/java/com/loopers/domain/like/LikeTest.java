package com.loopers.domain.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LikeTest {

    @DisplayName("좋아요 생성")
    @Nested
    class Create {

        @DisplayName("좋아요를 생성한다.")
        @Test
        void create() {
            //given
            String userId = "abc";
            Long productId = 1L;

            //when
            Like like = Like.create(userId, productId);

            //then
            assertThat(like.getId()).isNull();
            assertThat(like.getUserId()).isEqualTo("abc");
            assertThat(like.getProductId()).isEqualTo(1L);
        }


        @DisplayName("userId가 유효하지 않으면 예외를 반환한다.")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        void fail_userIdIsInvalid(String invalidId) {
            assertThatThrownBy(() -> Like.create(invalidId, 1L))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("User ID는 필수입니다.");
                });
        }

        @DisplayName("productId가 null 이면 예외를 반환한다.")
        @Test
        void fail_productIdIsNull() {
            assertThatThrownBy(() -> Like.create("abc", null))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                    assertThat(coreException.getMessage()).isEqualTo("Product ID는 필수입니다.");
                });
        }
    }
}
