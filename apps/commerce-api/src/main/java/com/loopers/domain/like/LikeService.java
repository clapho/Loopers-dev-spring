package com.loopers.domain.like;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public Like create(String userId, Long productId) {
        Like existingLike = likeRepository.findByUserIdAndProductId(userId, productId);
        if (existingLike != null) {
            return existingLike;
        }

        Like like = Like.create(userId, productId);
        return likeRepository.save(like);
    }

    @Transactional
    public void delete(String userId, Long productId) {
        likeRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public List<Like> findLikesByUser(String userId) {
        return likeRepository.findByUserId(userId);
    }

    public long countLikesByProduct(Long productId) {
        return likeRepository.countByProductId(productId);
    }

    public boolean isLikedByUser(String userId, Long productId) {
        return likeRepository.existsByUserIdAndProductId(userId, productId);
    }

    public long countLikesByUser(String userId) {
        return likeRepository.countByUserId(userId);
    }

    public List<Like> findLikesByUserIdWithPaging(String userId, int page, int size) {
        return likeRepository.findLikesByUserIdWithPaging(userId, page, size);
    }
}
