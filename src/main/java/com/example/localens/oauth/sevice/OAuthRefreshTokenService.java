package com.example.localens.oauth.sevice;

import com.example.localens.member.jwt.TokenProvider;
import com.example.localens.oauth.domain.OAuthRefreshToken;
import com.example.localens.oauth.repository.OAuthRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class OAuthRefreshTokenService {

    private final OAuthRefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    public Optional<OAuthRefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public OAuthRefreshToken createRefreshToken(String userId) {
        OAuthRefreshToken refreshToken = OAuthRefreshToken.builder()
                .token(generateRandomToken())
                .userId(userId)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public String refreshAccessToken(String refreshToken) {
        Optional<OAuthRefreshToken> optionalToken = findByToken(refreshToken);

        if (optionalToken.isEmpty()) {
            throw new OAuth2AuthenticationException("유효하지 않은 refresh token입니다.");
        }

        OAuthRefreshToken oAuthRefreshToken = optionalToken.get();

        //만료된 refresh token 체크
        if (oAuthRefreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            deleteRefreshToken(refreshToken);
            throw new OAuth2AuthenticationException("만료된 refresh token입니다.");
        }

        //새로운 access token 생성 로직
        String newAccessToken = generateNewAccessToken(oAuthRefreshToken.getUserId());

        return newAccessToken;

    }

    //새로운 access token 생성 로직
    private String generateNewAccessToken(String userId) {
        Authentication authentication = tokenProvider.getAuthentication(userId);
        String accessToken = tokenProvider.generateTokenDto(authentication).getAccessToken();

        return accessToken;
    }

    private String generateRandomToken() {
        return randomUUID().toString();
    }
}
