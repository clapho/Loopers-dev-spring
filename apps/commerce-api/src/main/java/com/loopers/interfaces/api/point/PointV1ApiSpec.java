package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Point V1 API", description = "사용자 Point API 입니다.")
public interface PointV1ApiSpec {

    @Operation(summary = "포인트 조회")
    ApiResponse<PointV1Dto.PointResponse> get(
        @RequestHeader("X-USER-ID") String userId
    );

    @Operation(summary = "포인트 충전")
    ApiResponse<PointV1Dto.PointResponse> charge(
        @RequestHeader("X-USER-ID") String userId,
        @RequestBody PointV1Dto.ChargeRequest chargeRequest
    );
}
