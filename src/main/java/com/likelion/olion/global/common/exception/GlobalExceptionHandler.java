package com.likelion.olion.global.common.exception;

import com.likelion.olion.global.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        return ResponseEntity.status(errorCode.status())
                .body(ApiResponse.error(errorCode.code(), errorCode.status(), exception.getMessage()));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(Exception exception) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(errorCode.status())
                .body(ApiResponse.error(errorCode.code(), errorCode.status(), errorCode.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.status())
                .body(ApiResponse.error(errorCode.code(), errorCode.status(), errorCode.message()));
    }
}
