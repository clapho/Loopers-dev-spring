package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @DisplayName("금액이 0 미만이면 예외를 반환한다.")
    @Test
    void fail_whenMoneyIsNegative() {

        assertThatThrownBy(() -> Money.of(BigDecimal.valueOf(-1)))
            .isInstanceOf(CoreException.class)
            .satisfies(exception -> {
                CoreException coreException = (CoreException) exception;
                assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                assertThat(coreException.getMessage()).isEqualTo("금액은 0 이상이어야 합니다.");
            });
    }

}
