package com.example.localens.oauth.util;

import jakarta.servlet.http.HttpServletRequest;

public class OAuthUtil {

    public static String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
