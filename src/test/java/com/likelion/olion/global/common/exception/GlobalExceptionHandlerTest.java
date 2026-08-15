package com.likelion.olion.global.common.exception;

import com.likelion.olion.global.common.response.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

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
    void handleUnexpectedExceptionReturns500AndLogsTheException() {
        RuntimeException exception = new RuntimeException("boom");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpectedException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(capturedRecords).hasSize(1);
        assertThat(capturedRecords.get(0).getLevel()).isEqualTo(Level.SEVERE);
        assertThat(capturedRecords.get(0).getThrown()).isSameAs(exception);
    }
}
