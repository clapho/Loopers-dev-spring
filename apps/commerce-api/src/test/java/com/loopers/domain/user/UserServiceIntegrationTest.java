package com.loopers.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("회원 가입")
    @Nested
    class Join{
        @Test
        @DisplayName("회원 가입시 User 저장이 수행된다.")
        void saveUser_whenSignUpIsSuccessful() {
            // given
            String userId = "soo";
            String name = "박수호";
            Gender gender = Gender.M;
            String email = "abc@gmail.com";
            String birth = "1995-03-01";

            // when
            User result = userService.signUp(userId, name, gender, email, birth);

            // then
            verify(userRepository, times(1)).existsByUserId(userId);
            verify(userRepository, times(1)).save(any(User.class));

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getUserId()).isEqualTo(userId),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getGender()).isEqualTo(gender),
                () -> assertThat(result.getEmail()).isEqualTo(email),
                () -> assertThat(result.getBirth()).isEqualTo(birth)
            );
        }

        @Test
        @DisplayName("이미 가입된 ID로 회원가입 시도 시, 실패한다.")
        void fail_whenUserIdAlreadyExists() {

            // given
            String existingUserId = "existUser";
            String name = "박수호";
            Gender gender = Gender.M;
            String email = "abc@gmail.com";
            String birth = "1995-03-01";

            userService.signUp(existingUserId, name, gender, email, birth);

            // when & then
            assertThatThrownBy(() -> userService.signUp(existingUserId, name, gender, email, birth))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.CONFLICT);
                    assertThat(coreException.getMessage()).isEqualTo("[userId = " + existingUserId + "] 이미 존재하는 사용자 ID입니다.");
                });

            verify(userRepository, times(2)).existsByUserId(existingUserId);
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    @DisplayName("내 정보 조회")
    @Nested
    class Find {
        @Test
        @DisplayName("해당 ID의 회원이 존재할 경우, 회원 정보가 반환된다.")
        void returnsUserInfo_whenUserExists() {
            //given
            String userId = "sooho";
            String name = "수호";
            Gender gender = Gender.M;
            String email = "abcd@gmail.com";
            String birth = "1996-03-01";

            userService.signUp(userId, name, gender, email, birth);

            //when
            User result = userService.findByUserId(userId);

            //then
            verify(userRepository, times(1)).findByUserId(userId);

            assertAll(
                () -> assertThat(result.getUserId()).isEqualTo(userId),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getGender()).isEqualTo(gender),
                () -> assertThat(result.getEmail()).isEqualTo(email),
                () -> assertThat(result.getBirth()).isEqualTo(birth)
            );
        }

        @Test
        @DisplayName("해당 ID의 회원이 존재하지 않을 경우, null 이 반환된다.")
        void returnsNull_whenUserNotExists() {
            //given
            String nonExistentUserId = "notfound";

            //when
            User result = userService.findByUserId(nonExistentUserId);

            //then
            verify(userRepository, times(1)).findByUserId(nonExistentUserId);
            assertThat(result).isNull();
        }
    }
}
