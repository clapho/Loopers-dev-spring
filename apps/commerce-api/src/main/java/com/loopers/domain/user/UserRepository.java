package com.loopers.domain.user;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {

    User save(User user);

    boolean existsByUserId(String userId);
}
