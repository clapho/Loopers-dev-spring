package com.loopers.domain.point;

import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository {

    Point findByUserId(String userId);

    Point save(Point point);
}
