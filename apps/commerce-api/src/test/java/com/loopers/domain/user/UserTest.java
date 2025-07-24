package com.loopers.domain.user;


import static com.loopers.domain.user.Gender.M;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

    @DisplayName("ID가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @ParameterizedTest
    @ValueSource(strings = {
        "박수호",
        "abc12345678",
        "abc%%%33",
        "",
    })
    void fail_whenIdFormatIsInvalid(String userId) {
        String name = "박수호";
        Gender gender = M;
        String email = "abc@gmail.com";
        String birth = "1995-03-01";

        assertThatThrownBy(() -> new User(userId, name, gender, email, birth))
            .isInstanceOf(CoreException.class)
            .satisfies(exception -> {
                CoreException coreException = (CoreException) exception;
                assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                assertThat(coreException.getMessage()).isEqualTo("ID는 영문 및 숫자 10자 이내여야 합니다.");
            });
    }

    @Test
    @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면 User 객체 생성에 실패한다.")
    void fail_whenEmailFormatIsInvalid() {
        String userId = "clap";
        String name = "박수호";
        Gender gender = M;
        String invalidEmail = "abc.gmail@com";
        String birth = "1995-03-01";

        assertThatThrownBy(() -> new User(userId, name, gender, invalidEmail, birth))
            .isInstanceOf(CoreException.class)
            .satisfies(exception -> {
                CoreException coreException = (CoreException) exception;
                assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                assertThat(coreException.getMessage()).isEqualTo("이메일은 xx@yy.zz 형식이어야 합니다.");
            });
    }

    @Test
    @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면 User 객체 생성에 실패한다.")
    void fail_whenBirthDateFormatIsInvalid() {
        String userId = "clap";
        String name = "박수호";
        Gender gender = M;
        String email = "abc@gmail.com";
        String birth = "1995.03.01";

        assertThatThrownBy(() -> new User(userId, name, gender, email, birth))
            .isInstanceOf(CoreException.class)
            .satisfies(exception -> {
                CoreException coreException = (CoreException) exception;
                assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                assertThat(coreException.getMessage()).isEqualTo("생년월일은 yyyy-MM-dd 형식이어야 합니다.");
            });
    }

}
