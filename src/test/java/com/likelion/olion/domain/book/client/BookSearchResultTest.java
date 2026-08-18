package com.likelion.olion.domain.book.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookSearchResultTest {
    @Test
    void ISBN13을_정규화하고_유효하지_않은_값은_비운다() {
        BookSearchResult normalized = new BookSearchResult(
                "책", "작가", null, null, null, null, "ALADIN",
                "978-89-364-3426-7", null, null);
        BookSearchResult invalid = new BookSearchResult(
                "책", "작가", null, null, null, null, "ALADIN",
                "8936434264", null, null);

        assertThat(normalized.isbn13()).isEqualTo("9788936434267");
        assertThat(invalid.isbn13()).isNull();
    }

    @Test
    void 필수_메타데이터_누락_여부를_판단한다() {
        BookSearchResult incomplete = new BookSearchResult(
                "책", "작가", null, "출판사", "소개", null,
                "ALADIN", "9788936434267", null, "소설");
        BookSearchResult complete = new BookSearchResult(
                "책", "작가", "이미지", "출판사", "소개", null,
                "ALADIN", "9788936434267", null, "소설");

        assertThat(incomplete.hasMissingMetadata()).isTrue();
        assertThat(complete.hasMissingMetadata()).isFalse();
    }
}
