package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface PointJpaRepository extends JpaRepository<Point, String> {

    Optional<Point> findByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")
    })
    @Query("SELECT p FROM Point p WHERE p.userId = :userId")
    Optional<Point> findByUserIdWithLock(@Param("userId") String userId);
}
