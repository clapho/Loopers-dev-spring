package com.loopers.domain.point;

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
}
