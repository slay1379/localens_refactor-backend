package com.example.localens.oauth.handler;

import com.example.localens.oauth.sevice.OAuthRefreshTokenService;
import com.example.localens.oauth.util.OAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private final OAuthRefreshTokenService refreshTokenService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException {
        String refreshtoken = OAuthUtil.extractTokenFromRequest(request);
        if (refreshtoken != null) {
            refreshTokenService.deleteRefreshToken(refreshtoken);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().flush();
    }
}
