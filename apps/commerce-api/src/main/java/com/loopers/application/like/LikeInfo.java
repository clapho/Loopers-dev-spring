package com.loopers.application.like;

public class LikeInfo {

    public static record Like(
        Long productId,
        Boolean isLiked,
        Long likeCount
    ) {}
}
