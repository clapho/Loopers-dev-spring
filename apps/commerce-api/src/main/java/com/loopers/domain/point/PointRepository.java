package com.loopers.domain.point;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository {

    Optional<Point> findByUserId(String userId);

    Optional<Point> findByUserIdWithLock(String userId);

    Point save(Point point);
}
