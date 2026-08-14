package com.likelion.olion.global.common.response;

import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모든 API의 공통 응답 형식")
public record ApiResponse<T>(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean isSuccess,
        @Schema(description = "응답 코드", example = "SUCCESS")
        String code,
        @Schema(description = "HTTP 상태 코드", example = "200")
        int httpStatus,
        @Schema(description = "응답 메시지", example = "요청이 성공했습니다.")
        String message,
        @Schema(description = "실제 응답 데이터")
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
