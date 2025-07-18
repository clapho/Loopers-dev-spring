package com.loopers.domain.user;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {

    User findByUserId(String userId);

    User save(User user);

    boolean existsByUserId(String userId);
}
