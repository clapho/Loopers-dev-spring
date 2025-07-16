package com.loopers.interfaces.api.user;

import jakarta.validation.constraints.NotNull;

public class UserV1Dto {
    public record SignUpRequest(
        @NotNull
        String userId,
        @NotNull
        String name,
        @NotNull
        GenderRequest gender,
        @NotNull
        String birth,
        @NotNull
        String email
    ) {

        enum GenderRequest {
            M,
            F
        }
    }

    public record UserResponse(
        String userId,
        String name,
        GenderResponse gender,
        String birth,
        String email
    ) { }

    enum GenderResponse {
        M,
        F
    }

}
