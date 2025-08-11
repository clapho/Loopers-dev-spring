package com.loopers.domain.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;


    @DisplayName("좋아요 추가")
    @Nested
    class Create {

        @DisplayName("새로운 좋아요를 추가한다.")
        @Test
        void create() {
            //given
            String userId = "user1";
            Long productId = 1L;

            when(likeRepository.findByUserIdAndProductId(userId, productId)).thenReturn(null);
            when(likeRepository.save(any(Like.class))).thenAnswer(invocation -> invocation.getArgument(0));

            //when
            likeService.like(userId, productId);

            //then
            verify(likeRepository).findByUserIdAndProductId(userId, productId);
            verify(likeRepository).save(any(Like.class));
        }

        @DisplayName("이미 좋아요가 존재하는 경우 기존 좋아요를 반환한다.")
        @Test
        void returnsExistingLike_whenAlreadyExists() {
            //given
            String userId = "user1";
            Long productId = 1L;
            Like existingLike = Like.create(userId, productId);

            when(likeRepository.findByUserIdAndProductId(userId, productId)).thenReturn(existingLike);

            //when
            Like result = likeService.like(userId, productId);

            //then
            assertThat(result).isEqualTo(existingLike);
            verify(likeRepository).findByUserIdAndProductId(userId, productId);
            verify(likeRepository, never()).save(any(Like.class));
        }
    }

    @DisplayName("좋아요 제거")
    @Nested
    class Delete {

        @DisplayName("좋아요를 제거한다.")
        @Test
        void delete() {
            //given
            String userId = "user1";
            Long productId = 1L;

            //when
            likeService.unlike(userId, productId);

            //then
            verify(likeRepository).deleteByUserIdAndProductId(userId, productId);
        }
    }

    @DisplayName("좋아요 조회")
    @Nested
    class Find {

        @DisplayName("사용자의 좋아요 목록을 조회한다")
        @Test
        void findLikesByUser() {
            //given
            String userId = "user123";
            List<Like> likes = List.of(
                Like.create(userId, 1L),
                Like.create(userId, 2L)
            );

            when(likeRepository.findByUserId(userId)).thenReturn(likes);

            //when
            List<Like> result = likeService.getAllByUser(userId);

            //then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(likes);
            verify(likeRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("상품의 좋아요 수를 조회한다")
        void countLikesByProduct() {
            //given
            Long productId = 1L;
            long expectedCount = 5L;

            when(likeRepository.countByProductId(productId)).thenReturn(expectedCount);

            //when
            long result = likeService.countByProduct(productId);

            //then
            assertThat(result).isEqualTo(expectedCount);
            verify(likeRepository).countByProductId(productId);
        }

        @Test
        @DisplayName("사용자가 특정 상품을 좋아요했는지 확인한다")
        void isLikedByUser() {
            //given
            String userId = "user123";
            Long productId = 1L;

            when(likeRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

            //when
            boolean result = likeService.isLiked(userId, productId);

            //then
            assertThat(result).isTrue();
            verify(likeRepository).existsByUserIdAndProductId(userId, productId);
        }
    }
}
