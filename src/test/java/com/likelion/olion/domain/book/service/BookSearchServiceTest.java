package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.dto.BookSearchResponse;
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
}
