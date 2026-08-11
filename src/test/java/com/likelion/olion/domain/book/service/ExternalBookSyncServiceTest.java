package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.client.BookSearchResult;
import com.likelion.olion.domain.book.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExternalBookSyncServiceTest {
    @Test
    void 동시_저장_충돌_시_이미_저장된_도서를_반환한다() {
        ExternalBookWriter writer = mock(ExternalBookWriter.class);
        ExternalBookSyncService service = new ExternalBookSyncService(writer);
        BookSearchResult result = new BookSearchResult(
                "아몬드", "손원평", null, "창비", null,
                "https://book.example/almond", "KAKAO", "9788936434267", "8936434264"
        );
        Book existing = mock(Book.class);
        when(writer.saveOrUpdate(result)).thenThrow(new DataIntegrityViolationException("duplicate"));
        when(writer.findExisting(result)).thenReturn(Optional.of(existing));

        List<Book> books = service.synchronize(List.of(result));

        assertThat(books).containsExactly(existing);
    }
}
