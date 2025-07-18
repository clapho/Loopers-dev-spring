package com.loopers.domain.user;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    @Transactional
    public User signUp(
        String userId,
        String name,
        Gender gender,
        String email,
        String birth
    ) {
        if (userRepository.existsByUserId(userId)) {
            throw new CoreException(ErrorType.CONFLICT,
                "[userId = " + userId + "] 이미 존재하는 사용자 ID입니다.");
        }

        User user = new User(userId, name, gender, email, birth);
        Point point = new Point(userId);
        pointRepository.save(point);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }
}
