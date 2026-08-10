package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.dto.BookDetailResponse;
import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookServiceTest {
    private final BookRepository repository = mock(BookRepository.class);
    private final BookService service = new BookService(repository);

    @Test
    void 도서_상세_정보를_반환한다() {
        Book book = mock(Book.class);
        when(book.getBookId()).thenReturn(1L);
        when(book.getTitle()).thenReturn("어린 왕자");
        when(book.getAuthor()).thenReturn("앙투안 드 생텍쥐페리");
        when(repository.findById(1L)).thenReturn(Optional.of(book));

        BookDetailResponse response = service.getBook(1L);

        assertThat(response.bookId()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("어린 왕자");
        assertThat(response.author()).isEqualTo("앙투안 드 생텍쥐페리");
        verify(repository).findById(1L);
    }

    @Test
    void 존재하지_않는_도서는_예외를_던진다() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBook(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("도서를 찾을 수 없습니다.");
    }
}
