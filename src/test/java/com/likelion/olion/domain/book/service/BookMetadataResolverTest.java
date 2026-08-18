package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.client.BookSearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookMetadataResolverTest {
    @Test
    void supplementsOnlyMissingFieldsForTheSameIsbn() {
        BookSearchResult aladin = new BookSearchResult(
                "아몬드", "손원평", null, null, "알라딘 소개", "aladin-url",
                "ALADIN", "9788936434267", "123", "소설");
        BookSearchResult kakao = new BookSearchResult(
                "아몬드", "손원평", "kakao-image", "창비", "카카오 소개", "kakao-url",
                "KAKAO", "9788936434267", "isbn-id", null);

        BookSearchResult merged = BookMetadataResolver.merge(List.of(aladin), List.of(kakao)).getFirst();

        assertThat(merged.coverImageUrl()).isEqualTo("kakao-image");
        assertThat(merged.publisher()).isEqualTo("창비");
        assertThat(merged.description()).isEqualTo("알라딘 소개");
        assertThat(merged.provider()).isEqualTo("ALADIN");
        assertThat(merged.externalUrl()).isEqualTo("aladin-url");
    }

    @Test
    void doesNotMergeDifferentIsbnBooks() {
        BookSearchResult aladin = new BookSearchResult(
                "아몬드", "손원평", null, null, null, null,
                "ALADIN", "9788936434267", "123", "소설");
        BookSearchResult kakao = new BookSearchResult(
                "아몬드", "손원평", "kakao-image", "창비", "소개", null,
                "KAKAO", "9780000000000", "isbn-id", null);

        BookSearchResult result = BookMetadataResolver.merge(List.of(aladin), List.of(kakao)).getFirst();

        assertThat(result.coverImageUrl()).isNull();
        assertThat(result.publisher()).isNull();
    }
}
