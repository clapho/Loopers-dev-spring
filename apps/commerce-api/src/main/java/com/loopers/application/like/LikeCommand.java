package com.loopers.application.like;

public class LikeCommand {

    public static record Add(
        String userId,
        Long productId
    ) {}

    public static record Remove(
        String userId,
        Long productId
    ) {}
}
