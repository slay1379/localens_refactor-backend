package com.example.localens.member.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyRegisteredException.class)
    public ResponseEntity<?> handleAlreadyRegisteredException(AlreadyRegisteredException e) {
        return ResponseEntity
                .status(401)
                .body(Map.of(
                        "message", e.getMessage(),
                        "code", 401
                ));
    }
}
