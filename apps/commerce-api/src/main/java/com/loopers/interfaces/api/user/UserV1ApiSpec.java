package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User V1 API", description = "Loopers 사용자 API 입니다.")
public interface UserV1ApiSpec {

    @Operation(summary = "회원 가입")
    ApiResponse<UserV1Dto.UserResponse> signUp(
        UserV1Dto.SignUpRequest signUpRequest
    );

    @Operation(summary = "내 정보 조회")
    ApiResponse<UserV1Dto.UserResponse> get(
        String userId
    );
}
