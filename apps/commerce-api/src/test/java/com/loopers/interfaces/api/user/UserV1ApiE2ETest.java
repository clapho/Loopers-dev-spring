package com.loopers.interfaces.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto.GenderResponse;
import com.loopers.interfaces.api.user.UserV1Dto.SignUpRequest;
import com.loopers.interfaces.api.user.UserV1Dto.SignUpRequest.GenderRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {

    private static final String ENDPOINT = "/api/v1/users";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @DisplayName("POST /api/v1/users")
    @Nested
    class Join {


        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenJoinIsSuccessful() {
            //given
            UserV1Dto.SignUpRequest signUpRequest = new SignUpRequest(
                "clap",
                "박수호",
                UserV1Dto.SignUpRequest.GenderRequest.M,
                "1995-03-01",
                "abc@gmail.com"
            );

            //when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(signUpRequest), responseType);

            //then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().data().name()).isEqualTo(signUpRequest.name()),
                () -> assertThat(response.getBody().data().gender()).isEqualTo(GenderResponse.M)
            );
        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenGenderIsMissing() {
            //given
            UserV1Dto.SignUpRequest signUpRequest = new SignUpRequest(
                "clap",
                "박수호",
                null,
                "1995-03-01",
                "abc@gmail.com"
            );

            //when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(signUpRequest), responseType);

            //then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                () -> assertThat(response.getBody().data()).isNull()
            );
        }
    }

    @DisplayName("GET /api/v1/users/{userId}")
    @Nested
    class Find {

        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenUserExists() {
            //given
            String userId = "exist";
            UserV1Dto.SignUpRequest signUpRequest = new SignUpRequest(
                userId,
                "성공",
                GenderRequest.M,
                "1995-03-02",
                "abcde@gmail.com"
            );

            testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(signUpRequest),
                new ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>>() {}
            );

            //when
            String getEndpoint = ENDPOINT + "/" + userId;
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType =
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(getEndpoint, HttpMethod.GET, null, responseType);

            //then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                () -> assertThat(response.getBody().data()).isNotNull(),
                () -> assertThat(response.getBody().data().userId()).isEqualTo(userId),
                () -> assertThat(response.getBody().data().name()).isEqualTo("성공"),
                () -> assertThat(response.getBody().data().gender()).isEqualTo(GenderResponse.M),
                () -> assertThat(response.getBody().data().birth()).isEqualTo("1995-03-02"),
                () -> assertThat(response.getBody().data().email()).isEqualTo("abcde@gmail.com")
            );
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, `404 Not Found` 응답을 반환한다.")
        @Test
        void returns404_whenUserNotExists() {
            //given
            String nonExistentUserId = "notexist";

            //when
            String getEndpoint = ENDPOINT + "/" + nonExistentUserId;
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType =
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(getEndpoint, HttpMethod.GET, null, responseType);

            //then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                () -> assertThat(response.getBody().meta().message()).contains("사용자가 존재하지 않습니다."),
                () -> assertThat(response.getBody().data()).isNull()
            );
        }
    }
}
