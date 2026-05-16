package com.salonnbooking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.AuthToken;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenAndIsActiveTrue(String token);

    List<AuthToken> findByUserIdAndIsActiveTrue(Long userId);

    List<AuthToken> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime expiresAt);
}
