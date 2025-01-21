package com.example.localens.customfeature.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response<T> {
    private final boolean success;
    private final T data;
    private final String message;

    public static <T> Response<T> success(T data) {
        return new Response<>(true, data, null);
    }

    public static <T> Response<T> success(String message) {
        return new Response<>(true, null, message);
    }

    public static <T> Response<T> error(String message) {
        return new Response<>(false, null, message);
    }
}
