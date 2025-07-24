package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, String> {

    boolean existsByUserId(String userId);

    Optional<User> findByUserId(String userId);
}
