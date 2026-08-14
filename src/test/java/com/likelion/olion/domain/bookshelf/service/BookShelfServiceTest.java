package com.likelion.olion.domain.bookshelf.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.domain.bookshelf.dto.BookShelfRequest;
import com.likelion.olion.domain.bookshelf.dto.BookShelfSaveResponse;
import com.likelion.olion.domain.bookshelf.entity.BookStatus;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookShelfServiceTest {
    private final UserBookRepository userBookRepository = mock(UserBookRepository.class);
    private final BookRepository bookRepository = mock(BookRepository.class);
    private final BookShelfService service = new BookShelfService(userBookRepository, bookRepository);

    @Test
    void 책장에_도서를_저장하면_읽기_전_상태로_생성한다() {
        Book book = mock(Book.class);
        when(book.getBookId()).thenReturn(5L);
        when(bookRepository.findById(5L)).thenReturn(Optional.of(book));
        when(userBookRepository.existsByUserIdAndBookBookId(1L, 5L)).thenReturn(false);
        UserBook saved = mock(UserBook.class);
        when(saved.getUserBookId()).thenReturn(30L);
        when(saved.getStatus()).thenReturn(BookStatus.BEFORE_READING);
        when(userBookRepository.save(org.mockito.ArgumentMatchers.any(UserBook.class))).thenReturn(saved);

        BookShelfSaveResponse response = service.addBook(1L, new BookShelfRequest(5L));

        assertThat(response.userBookId()).isEqualTo(30L);
        assertThat(response.status()).isEqualTo("BEFORE_READING");
    }

    @Test
    void 이미_저장한_도서는_중복_저장할_수_없다() {
        Book book = mock(Book.class);
        when(book.getBookId()).thenReturn(5L);
        when(bookRepository.findById(5L)).thenReturn(Optional.of(book));
        when(userBookRepository.existsByUserIdAndBookBookId(1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.addBook(1L, new BookShelfRequest(5L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 책장에 담긴 도서입니다.");
    }

    @Test
    void 잘못된_도서_상태는_거부한다() {
        assertThatThrownBy(() -> service.changeStatus(1L, 30L, "INVALID"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("유효하지 않은 status 값입니다.");
    }
}
