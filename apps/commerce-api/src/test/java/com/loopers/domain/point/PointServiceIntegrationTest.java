package com.loopers.domain.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserService userService;

    @MockitoSpyBean
    private PointRepository pointRepository;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트 조회")
    @Nested
    class Get {

        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsPointAmount_whenUserExists() {
            //given
            String userId = "exist";
            userService.signUp(
                userId,
                "성공",
                Gender.M,
                "abc@gmail.com",
                "2000-01-01"
            );

            //when
            Long point = pointService.getAmount(userId);

            //then
            verify(pointRepository, times(1)).findByUserId(userId);
            assertThat(point).isEqualTo(0L);
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNull_whenUserNotExists() {
            //given
            String userId = "notexist";

            //when
            Long point = pointService.getAmount(userId);

            //then
            verify(userRepository, times(1)).existsByUserId(userId);
            verify(pointRepository, never()).findByUserId(userId);
            assertThat(point).isNull();
        }
    }

    @DisplayName("포인트 충전")
    @Nested
    class Charge {

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void failToCharge_whenUserNotExists() {
            //given
            String nonExistentUserId = "notexist";
            Long chargeAmount = 1000L;

            //when & then
            assertThatThrownBy(() -> pointService.charge(nonExistentUserId, chargeAmount))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
                    assertThat(coreException.getMessage()).isEqualTo(
                        "[userId = " + nonExistentUserId + "] 해당 유저가 존재하지 않습니다.");
                });

            verify(userRepository, times(1)).existsByUserId(nonExistentUserId);
            verify(pointRepository, never()).findByUserId(nonExistentUserId);
        }
    }

}
