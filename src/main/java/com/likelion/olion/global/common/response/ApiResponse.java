package com.likelion.olion.global.common.response;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        boolean isSuccess,
        String code,
        int httpStatus,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return success("SUCCESS", HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> success(
            String code,
            HttpStatus status,
            String message,
            T data
    ) {
        return new ApiResponse<>(true, code, status.value(), message, data);
    }

    public static <T> ApiResponse<T> error(
            String code,
            HttpStatus status,
            String message
    ) {
        return new ApiResponse<>(false, code, status.value(), message, null);
    }
}
