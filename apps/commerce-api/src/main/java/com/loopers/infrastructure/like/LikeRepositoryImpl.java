package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    public Like save(Like like) {
        return likeJpaRepository.save(like);
    }

    @Override
    public Like findByUserIdAndProductId(String userId, Long productId) {
        return likeJpaRepository.findByUserIdAndProductId(userId, productId).orElse(null);
    }

    @Override
    public void deleteByUserIdAndProductId(String userId, Long productId) {
        likeJpaRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<Like> findByUserId(String userId) {
        return likeJpaRepository.findByUserId(userId);
    }

    @Override
    public long countByProductId(Long productId) {
        return likeJpaRepository.countByProductId(productId);
    }

    @Override
    public boolean existsByUserIdAndProductId(String userId, Long productId) {
        return likeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }
}
