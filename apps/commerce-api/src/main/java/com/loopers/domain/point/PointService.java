package com.loopers.domain.point;

import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Long getPointAmount(String userId) {
        if (!userRepository.existsByUserId(userId)) {
            return null;
        }

        Point point = pointRepository.findByUserId(userId);

        if (point == null) {
            throw new CoreException(ErrorType.NOT_FOUND,
                "[userId = " + userId + "] 포인트 정보를 찾을 수 없습니다."
            );
        }

        return point.getAmount();
    }

    @Transactional
    public Long chargePoint(String userId, Long chargeAmount) {
        if (!userRepository.existsByUserId(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND,
                "[userId = " + userId + "] 해당 유저가 존재하지 않습니다.");
        }

        Point point = pointRepository.findByUserId(userId);

        if (point == null) {
            throw new CoreException(ErrorType.NOT_FOUND,
                "[userId = " + userId + "] 포인트 정보를 찾을 수 없습니다.");
        }

        point.charge(chargeAmount);
        pointRepository.save(point);

        return point.getAmount();
    }
}
