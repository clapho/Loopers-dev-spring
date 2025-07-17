package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "point")
@Getter
public class Point {
    @Id
    private String userId;
    private Long amount;

    protected Point() {}

    public Point(String userId) {
        this.userId = userId;
        this.amount = 0L;
    }

    public void charge(Long chargeAmount) {
        if (chargeAmount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST,
                "0 이하의 값은 충전이 불가합니다."
            );
        }

        this.amount += chargeAmount;
    }
}
