package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeJpaRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndProductId(String userId, Long productId);

    void deleteByUserIdAndProductId(String userId, Long productId);

    boolean existsByUserIdAndProductId(String userId, Long productId);

    List<Like> findByUserId(String userId);

    long countByProductId(Long productId);

    long countByUserId(String userId);

    @Query("SELECT l FROM Like l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    Page<Like> findLikesByUserIdOrderByCreatedAt(@Param("userId") String userId, Pageable pageable);
}
