package com.likelion.olion.global.common.exception;

import com.likelion.olion.global.common.response.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final List<LogRecord> capturedRecords = new java.util.ArrayList<>();
    private Handler capturingHandler;

    @BeforeEach
    void setUp() {
        capturingHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                capturedRecords.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        Logger.getLogger(GlobalExceptionHandler.class.getName()).addHandler(capturingHandler);
    }

    @AfterEach
    void tearDown() {
        Logger.getLogger(GlobalExceptionHandler.class.getName()).removeHandler(capturingHandler);
    }

    @Test
    void handleBusinessExceptionReturnsMappedStatusAndMessage() {
        BusinessException exception = new BusinessException(ErrorCode.NOT_FOUND, "찾을 수 없습니다.");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("찾을 수 없습니다.");
    }

    @Test
    void handleValidationExceptionReturnsFieldSpecificMessage() {
        FieldError fieldError = new FieldError(
                "essayCreateRequest", "reflectionIds", "에세이 생성에는 사유 30개가 필요합니다.");
        BindingResult bindingResult = mock(BindingResult.class);
        given(bindingResult.getFieldErrors()).willReturn(List.of(fieldError));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        given(exception.getBindingResult()).willReturn(bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("에세이 생성에는 사유 30개가 필요합니다.");
    }

    @Test
    void handleValidationExceptionFallsBackToGenericMessageWhenNoFieldError() {
        BindingResult bindingResult = mock(BindingResult.class);
        given(bindingResult.getFieldErrors()).willReturn(List.of());
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        given(exception.getBindingResult()).willReturn(bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(exception);

        assertThat(response.getBody().message()).isEqualTo(ErrorCode.INVALID_INPUT.message());
    }

    @Test
    void handleUnexpectedExceptionReturns500AndLogsTheException() {
        RuntimeException exception = new RuntimeException("boom");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpectedException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(capturedRecords).hasSize(1);
        assertThat(capturedRecords.get(0).getLevel()).isEqualTo(Level.SEVERE);
        assertThat(capturedRecords.get(0).getThrown()).isSameAs(exception);
    }
}
