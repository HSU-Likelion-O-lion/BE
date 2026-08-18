package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.entity.BookSummaryStatus;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.global.ai.AiTextGenerator;
import com.likelion.olion.global.ai.PromptTemplateLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BookSummaryServiceTest {
    private final BookRepository bookRepository = mock(BookRepository.class);
    private final AiTextGenerator aiTextGenerator = mock(AiTextGenerator.class);
    private final BookSummaryService service = new BookSummaryService(
            bookRepository, aiTextGenerator, new PromptTemplateLoader());

    @Test
    void generatesAndStoresSummaryOnlyOnce() {
        Book book = Book.fromExternal(
                "아몬드", "손원평", null, "창비", "원본 책 소개", null,
                "ALADIN", "9788936434267", "123", "소설");
        given(aiTextGenerator.generate(eq(null), eq("book-summary"), anyString(), eq("원본 책 소개")))
                .willReturn("AI가 요약한 책 소개");

        String summary = service.ensureSummary(book);

        assertThat(summary).isEqualTo("AI가 요약한 책 소개");
        assertThat(book.getAiSummary()).isEqualTo("AI가 요약한 책 소개");
        assertThat(book.getSummaryStatus()).isEqualTo(BookSummaryStatus.COMPLETED);
        assertThat(book.getSummaryPromptVersion()).isEqualTo("v1");
        verify(bookRepository).save(book);
    }

    @Test
    void usesOriginalDescriptionAsFallbackWhenAiIsUnavailable() {
        Book book = Book.fromExternal(
                "아몬드", "손원평", null, "창비", "원본 책 소개", null,
                "ALADIN", "9788936434267", "123", "소설");
        given(aiTextGenerator.generate(eq(null), eq("book-summary"), anyString(), eq("원본 책 소개")))
                .willReturn("원본 책 소개");

        service.ensureSummary(book);

        assertThat(book.getAiSummary()).isEqualTo("원본 책 소개");
        assertThat(book.getSummaryStatus()).isEqualTo(BookSummaryStatus.FALLBACK);
    }
}
