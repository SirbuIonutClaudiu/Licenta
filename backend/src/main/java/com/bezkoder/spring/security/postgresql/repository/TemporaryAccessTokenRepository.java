package com.bezkoder.spring.security.postgresql.repository;

import com.bezkoder.spring.security.postgresql.models.TemporaryAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporaryAccessTokenRepository extends JpaRepository<TemporaryAccessToken, Long> {
    TemporaryAccessToken findByMemberId(Long id);
    boolean existsByMemberId(Long memberId);
}
