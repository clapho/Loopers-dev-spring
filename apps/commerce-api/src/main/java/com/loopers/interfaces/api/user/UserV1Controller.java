package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto.UserResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec{

    private final UserService userService;

    public UserV1Controller(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(
        @Valid @RequestBody UserV1Dto.SignUpRequest signUpRequest
    ) {

        User user = userService.signUp(
            signUpRequest.userId(),
            signUpRequest.name(),
            signUpRequest.gender().toDomainGender(),
            signUpRequest.email(),
            signUpRequest.birth()
        );

        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(user);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> get(@PathVariable String userId) {
        User user = userService.get(userId);

        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND,
                "[userId = " + userId + "] 사용자를 찾을 수 없습니다."
            );
        }

        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(user);

        return ApiResponse.success(response);
    }
}
