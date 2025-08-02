package com.loopers.domain.like;

import java.util.List;

public interface LikeRepository {

    Like save(Like like);

    Like findByUserIdAndProductId(String userId, Long productId);

    void deleteByUserIdAndProductId(String userId, Long productId);

    List<Like> findByUserId(String userId);

    List<Like> findLikesByUserIdWithPaging(String userId, int page, int size);

    long countByProductId(Long productId);

    boolean existsByUserIdAndProductId(String userId, Long productId);

    long countByUserId(String userId);
}
