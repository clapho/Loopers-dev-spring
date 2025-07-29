package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "brand")
@Getter
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    protected Brand() {}

    private Brand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Brand create(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "브랜드명은 필수입니다."
            );
        }

        if (description == null || description.trim().isEmpty()) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "브랜드 설명은 필수입니다."
            );
        }

        return new Brand(name, description);
    }
}
