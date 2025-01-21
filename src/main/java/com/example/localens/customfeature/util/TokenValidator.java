package com.example.localens.customfeature.util;

import com.example.localens.member.jwt.TokenProvider;
import com.example.localens.customfeature.exception.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenValidator {
    private final TokenProvider tokenProvider;

    public UUID validateAndGetUserUuid(String authorizationHeader) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid token");
        }

        UUID userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            throw new UnauthorizedException("USer not found");
        }

        return userUuid;
    }
}
