package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.point.PointV1Dto.ChargeRequest;
import com.loopers.interfaces.api.point.PointV1Dto.PointResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointV1Controller implements PointV1ApiSpec{

    private final PointService pointService;

    @GetMapping
    @Override
    public ApiResponse<PointResponse> getPoint(@RequestHeader ("X-USER-ID") String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 값이 비어있습니다.");
        }

        Long amount = pointService.getPointAmount(userId);

        if (amount == null) {
            throw new CoreException(ErrorType.NOT_FOUND,
                "[userId = " + userId + "] 사용자를 찾을 수 없습니다.");
        }

        return ApiResponse.success(new PointResponse(amount));
    }

    @PostMapping
    @Override
    public ApiResponse<PointResponse> chargePoint(
        @RequestHeader("X-USER-ID") String userId,
        @RequestBody ChargeRequest chargeRequest
    ) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 값이 비어있습니다.");
        }

        Long totalAmount = pointService.chargePoint(userId, chargeRequest.amount());
        return ApiResponse.success(new PointResponse(totalAmount));
    }


}
