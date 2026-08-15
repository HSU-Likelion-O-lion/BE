package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.client.BookSearchResult;
import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.repository.BookRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalBookWriterTest {
    private final BookRepository repository = mock(BookRepository.class);
    private final ExternalBookWriter writer = new ExternalBookWriter(repository);

    @Test
    void ISBN13으로_기존_도서를_찾아_갱신한다() {
        Book existing = Book.fromExternal(
                "이전 제목", "손원평", null, "창비", null,
                "https://old.example", "KAKAO", "9788936434267", "8936434264", null
        );
        BookSearchResult result = result();
        when(repository.findByIsbn13("9788936434267")).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        Book saved = writer.saveOrUpdate(result);

        assertThat(saved).isSameAs(existing);
        assertThat(saved.getTitle()).isEqualTo("아몬드");
        assertThat(saved.getCoverImageUrl()).isEqualTo("https://image.example/almond.jpg");
        verify(repository).saveAndFlush(existing);
    }

    @Test
    void 식별자가_일치하지_않으면_새_도서를_저장한다() {
        BookSearchResult result = result();
        when(repository.findByIsbn13("9788936434267")).thenReturn(Optional.empty());
        when(repository.findByProviderAndProviderBookId("KAKAO", "8936434264"))
                .thenReturn(Optional.empty());
        when(repository.findByExternalUrl("https://book.example/almond")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book saved = writer.saveOrUpdate(result);

        assertThat(saved.getTitle()).isEqualTo("아몬드");
        assertThat(saved.getIsbn13()).isEqualTo("9788936434267");
        assertThat(saved.getProviderBookId()).isEqualTo("8936434264");
    }

    private BookSearchResult result() {
        return new BookSearchResult(
                "아몬드", "손원평", "https://image.example/almond.jpg", "창비",
                "책 소개", "https://book.example/almond", "KAKAO",
                "9788936434267", "8936434264", null
        );
    }
}
