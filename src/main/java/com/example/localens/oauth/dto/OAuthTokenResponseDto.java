package com.example.localens.oauth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthTokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}
