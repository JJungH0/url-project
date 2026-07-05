package com.url.jjung.global.util;

public record ApiResp<T>(
        boolean success,
        T data,
        String message
) {
    public static <T> ApiResp<T> success(T data, String message) {
        return new ApiResp<>(true, data, message);
    }

    public static <T> ApiResp<T> error(String message) {
        return new ApiResp<>(false, null, message);
    }
}
