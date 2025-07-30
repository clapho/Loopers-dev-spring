package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;

@Entity
@Table(
    name = "likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "productId"})
)
@Getter
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private Long productId;

    private LocalDateTime createdAt;

    protected Like() {
    }

    private Like(String userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
        this.createdAt = LocalDateTime.now();
    }

    public static Like create(String userId, Long productId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "User ID는 필수입니다."
            );
        }

        if (productId == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "Product ID는 필수입니다."
            );
        }

        return new Like(userId, productId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Like like = (Like) obj;
        return Objects.equals(userId, like.userId) &&
            Objects.equals(productId, like.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId);
    }
}
