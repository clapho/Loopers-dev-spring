package com.loopers.interfaces.api.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
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
            M, F;

            public Gender toDomainGender() {
                return switch (this) {
                    case M -> Gender.M;
                    case F -> Gender.F;
                };
            }
        }
    }

    public record UserResponse(
        String userId,
        String name,
        GenderResponse gender,
        String birth,
        String email
    ) {

        public static UserResponse from(User user) {
            return new UserResponse(
                user.getUserId(),
                user.getName(),
                GenderResponse.from(user.getGender()),
                user.getBirth(),
                user.getEmail()
            );
        }
    }


    enum GenderResponse {
        M, F;

        public static GenderResponse from(Gender gender) {
            return switch (gender) {
                case M -> GenderResponse.M;
                case F -> GenderResponse.F;
            };
        }

        public Gender toDomainGender() {
            return switch (this) {
                case M -> Gender.M;
                case F -> Gender.F;
            };
        }
    }

}
