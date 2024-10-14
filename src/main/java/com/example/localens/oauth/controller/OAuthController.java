package com.example.localens.oauth.controller;

import com.example.localens.oauth.sevice.OAuthRefreshTokenService;
import com.example.localens.oauth.sevice.OAuthService;
import com.example.localens.oauth.util.OAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthRefreshTokenService refreshTokenService;

    @GetMapping("/refresh-token")
    public String refreshAccessToken(String refreshToken) {
        //refreshToken을 사용하여 새로운 access token 발급 로직 구현
        return refreshTokenService.refreshAccessToken(refreshToken);
    }
}
