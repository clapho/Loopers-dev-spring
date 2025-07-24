package com.loopers.domain.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PointTest {

    @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void failToCharge_whenChargeAmountIsZeroOrNegative(Long chargeAmount) {
        //given
        Point point = new Point("clap");

        //when & then
        assertThatThrownBy(() -> point.charge(chargeAmount))
            .isInstanceOf(CoreException.class)
            .satisfies(exception -> {
                CoreException coreException = (CoreException) exception;
                assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                assertThat(coreException.getMessage()).isEqualTo("0 이하의 값은 충전이 불가합니다.");
            });
    }
}
