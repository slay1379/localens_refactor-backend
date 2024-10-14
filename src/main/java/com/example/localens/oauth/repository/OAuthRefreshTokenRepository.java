package com.example.localens.oauth.repository;

import com.example.localens.oauth.domain.OAuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthRefreshTokenRepository extends JpaRepository<OAuthRefreshToken, Long> {
    Optional<OAuthRefreshToken> findByToken(String token);

    void deleteByToken(String token);
}
