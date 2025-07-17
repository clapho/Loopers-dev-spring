package com.loopers.domain.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
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
            Long point = pointService.getPointAmount(userId);

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
            Long point = pointService.getPointAmount(userId);

            //then
            verify(userRepository, times(1)).existsByUserId(userId);
            verify(pointRepository, never()).findByUserId(userId);
            assertThat(point).isNull();
        }
    }

}
