package com.example.localens.customfeature.util;

import com.example.localens.member.jwt.TokenProvider;
import com.example.localens.customfeature.exception.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenValidator {
    private final TokenProvider tokenProvider;

    public UUID validateAndGetUserUuid(String authorizationHeader) {
        log.info("Received authorization header: {}", authorizationHeader);

        String token = tokenProvider.extractToken(authorizationHeader);
        log.info("Extracted token: {}", token);

        if (token == null) {
            log.error("Token extraction failed");
            throw new UnauthorizedException("Authorization header is missing or invalid");
        }

        boolean isValid = tokenProvider.validateToken(token);
        log.info("Token validation result: {}", isValid);

        if (!isValid) {
            log.error("Token validation failed");
            throw new UnauthorizedException("Token validation failed");
        }

        try {
            UUID userUuid = tokenProvider.getCurrentUuid(token);
            log.info("Extracted UUID: {}", userUuid);

            if (userUuid == null) {
                log.error("UUID is null");
                throw new UnauthorizedException("Invalid user UUID in token");
            }

            return userUuid;
        } catch (IllegalArgumentException e) {
            log.error("Error parsing UUID: {}", e.getMessage());
            throw new UnauthorizedException("Invalid UUID format in token");
        }
    }
}
