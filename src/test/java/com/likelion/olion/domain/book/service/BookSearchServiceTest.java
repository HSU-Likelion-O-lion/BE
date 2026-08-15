package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.dto.BookSearchResponse;
import com.likelion.olion.domain.book.client.BookSearchProvider;
import com.likelion.olion.domain.book.client.BookSearchResult;
import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookSearchServiceTest {
    private final BookRepository repository = mock(BookRepository.class);
    private final BookService service = new BookService(repository);

    @Test
    void 제목이나_저자로_도서를_검색한다() {
        Book book = mock(Book.class);
        when(book.getBookId()).thenReturn(5L);
        when(book.getTitle()).thenReturn("아몬드");
        when(book.getAuthor()).thenReturn("손원평");
        when(book.getCoverImageUrl()).thenReturn("https://cdn.olion.com/book/5.png");
        when(repository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("아몬드", "아몬드"))
                .thenReturn(List.of(book));

        BookSearchResponse response = service.searchBooks(" 아몬드 ");

        assertThat(response.books()).hasSize(1);
        assertThat(response.books().getFirst().bookId()).isEqualTo(5L);
        assertThat(response.books().getFirst().title()).isEqualTo("아몬드");
        verify(repository).findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("아몬드", "아몬드");
    }

    @Test
    void 검색_결과가_없으면_빈_배열을_반환한다() {
        when(repository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("없는 책", "없는 책"))
                .thenReturn(List.of());

        BookSearchResponse response = service.searchBooks("없는 책");

        assertThat(response.books()).isEmpty();
    }

    @Test
    void 검색어가_없으면_예외를_던진다() {
        assertThatThrownBy(() -> service.searchBooks("   "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("검색어를 입력해주세요.");
    }

    @Test
    void 외부_제공자의_검색_결과를_공통_응답으로_변환한다() {
        BookSearchProvider provider = mock(BookSearchProvider.class);
        ExternalBookSyncService syncService = mock(ExternalBookSyncService.class);
        BookService externalService = new BookService(repository, List.of(provider), syncService);
        BookSearchResult result = new BookSearchResult(
                "아몬드", "손원평", "https://image.example/almond.jpg",
                "창비", "책 소개", "https://book.example/almond", "KAKAO",
                "9788936434267", "9788936434267", null
        );
        Book savedBook = mock(Book.class);
        when(savedBook.getBookId()).thenReturn(5L);
        when(savedBook.getTitle()).thenReturn("아몬드");
        when(savedBook.getAuthor()).thenReturn("손원평");
        when(savedBook.getCoverImageUrl()).thenReturn("https://image.example/almond.jpg");
        when(provider.search("아몬드")).thenReturn(List.of(result));
        when(syncService.synchronize(List.of(result))).thenReturn(List.of(savedBook));

        BookSearchResponse response = externalService.searchBooks(" 아몬드 ");

        assertThat(response.books()).singleElement()
                .satisfies(book -> {
                    assertThat(book.bookId()).isEqualTo(5L);
                    assertThat(book.title()).isEqualTo("아몬드");
                    assertThat(book.coverImageUrl()).isEqualTo("https://image.example/almond.jpg");
                });
        verify(provider).search("아몬드");
    }

    @Test
    void 외부_제공자_실패_시_로컬_DB로_검색한다() {
        BookSearchProvider provider = mock(BookSearchProvider.class);
        ExternalBookSyncService syncService = mock(ExternalBookSyncService.class);
        BookService externalService = new BookService(repository, List.of(provider), syncService);
        when(provider.search("아몬드")).thenThrow(new RuntimeException("provider unavailable"));
        when(repository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("아몬드", "아몬드"))
                .thenReturn(List.of());

        BookSearchResponse response = externalService.searchBooks("아몬드");

        assertThat(response.books()).isEmpty();
        verify(repository).findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("아몬드", "아몬드");
    }
}
