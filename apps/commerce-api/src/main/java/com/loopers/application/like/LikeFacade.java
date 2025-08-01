package com.loopers.application.like;

import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeFacade {

    private final LikeService likeService;
    private final ProductService productService;
    private final UserService userService;

    @Transactional
    public LikeInfo.Like addLike(LikeCommand.Add command) {
        validateLikeCommand(command.userId(), command.productId());

        boolean alreadyLiked = likeService.isLikedByUser(command.userId(), command.productId());

        if (!alreadyLiked) {
            likeService.create(command.userId(), command.productId());
        }

        return buildLikeInfo(command.productId(), true);
    }

    @Transactional
    public LikeInfo.Like removeLike(LikeCommand.Remove command) {
        validateLikeCommand(command.userId(), command.productId());

        boolean currentlyLiked = likeService.isLikedByUser(command.userId(), command.productId());

        if (currentlyLiked) {
            likeService.delete(command.userId(), command.productId());
        }

        return buildLikeInfo(command.productId(), false);
    }

    private void validateLikeCommand(String userId, Long productId) {
        validateUserExists(userId);
        validateProductExists(productId);
    }

    private void validateUserExists(String userId) {
        User user = userService.findByUserId(userId);
        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "사용자가 존재하지 않습니다.");
        }
    }

    private Product validateProductExists(Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다.");
        }
        return product;
    }

    private LikeInfo.Like buildLikeInfo(Long productId, boolean isLiked) {
        long likeCount = likeService.countLikesByProduct(productId);
        return new LikeInfo.Like(productId, isLiked, likeCount);
    }
}
