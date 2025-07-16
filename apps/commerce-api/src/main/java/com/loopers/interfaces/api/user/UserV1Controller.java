package com.loopers.interfaces.api.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto.GenderResponse;
import jakarta.validation.Valid;
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

        Gender gender = convertGender(signUpRequest.gender());

        User user = userService.signUp(
            signUpRequest.userId(),
            signUpRequest.name(),
            gender,
            signUpRequest.email(),
            signUpRequest.birth()
        );

        UserV1Dto.UserResponse response = new UserV1Dto.UserResponse(
            user.getUserId(),
            user.getName(),
            convvertGenderResponse(user.getGender()),
            user.getBirth(),
            user.getEmail()
        );

        return ApiResponse.success(response);
    }

    private Gender convertGender(UserV1Dto.SignUpRequest.GenderRequest genderRequest) {
        return switch (genderRequest) {
            case M -> Gender.M;
            case F -> Gender.F;
        };
    }

    private UserV1Dto.GenderResponse convvertGenderResponse(Gender gender) {
        return switch (gender) {
            case M -> GenderResponse.M;
            case F -> GenderResponse.F;
        };
    }
}
