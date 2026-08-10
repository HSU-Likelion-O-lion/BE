package com.likelion.olion.global.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiResponseTest {
    @Test
    void 성공_응답은_명세의_공통필드를_채운다() {
        ApiResponse<String> response = ApiResponse.success(
                "SUCCESS_EMPTY",
                HttpStatus.OK,
                "검색 결과가 없습니다.",
                "empty"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.code()).isEqualTo("SUCCESS_EMPTY");
        assertThat(response.httpStatus()).isEqualTo(200);
        assertThat(response.message()).isEqualTo("검색 결과가 없습니다.");
        assertThat(response.data()).isEqualTo("empty");
    }

    @Test
    void 오류_응답은_data가_null이다() {
        ApiResponse<Void> response = ApiResponse.error(
                "BOOK_404_2",
                HttpStatus.NOT_FOUND,
                "도서를 찾을 수 없습니다."
        );

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.httpStatus()).isEqualTo(404);
        assertThat(response.data()).isNull();
    }
}
