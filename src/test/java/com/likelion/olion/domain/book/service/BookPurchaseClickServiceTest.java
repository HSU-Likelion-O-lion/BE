package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.dto.BookPurchaseClickResponse;
import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.entity.BookPurchaseClick;
import com.likelion.olion.domain.book.repository.BookPurchaseClickRepository;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookPurchaseClickServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookPurchaseClickRepository purchaseClickRepository;

    @Test
    void recordsEveryClickAndReturnsRedirectUrl() {
        BookPurchaseClickService service = createService();
        Book book = org.mockito.Mockito.mock(Book.class);
        given(book.getExternalUrl())
                .willReturn("  https://book.store.com/item/5?ref=olion  ");
        given(bookRepository.findById(5L)).willReturn(Optional.of(book));

        BookPurchaseClickResponse response = service.recordClick(7L, 5L);

        assertThat(response.redirectUrl())
                .isEqualTo("https://book.store.com/item/5?ref=olion");
        ArgumentCaptor<BookPurchaseClick> captor = ArgumentCaptor.forClass(
                BookPurchaseClick.class);
        verify(purchaseClickRepository).save(captor.capture());
        assertThat(captor.getValue().getBook()).isSameAs(book);
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
        assertThat(captor.getValue().getRedirectUrl())
                .isEqualTo("https://book.store.com/item/5?ref=olion");
        assertThat(captor.getValue().getClickedAt()).isNotNull();
    }

    @Test
    void rejectsMissingBook() {
        BookPurchaseClickService service = createService();
        given(bookRepository.findById(5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordClick(7L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(businessException.getMessage()).isEqualTo("도서를 찾을 수 없습니다.");
                });
        verify(purchaseClickRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsBookWithoutPurchaseLink() {
        BookPurchaseClickService service = createService();
        Book book = org.mockito.Mockito.mock(Book.class);
        given(book.getExternalUrl()).willReturn(" ");
        given(bookRepository.findById(5L)).willReturn(Optional.of(book));

        assertThatThrownBy(() -> service.recordClick(7L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode())
                            .isEqualTo(ErrorCode.UNPROCESSABLE_ENTITY);
                    assertThat(businessException.getMessage())
                            .isEqualTo("구매 링크를 제공하지 않는 도서입니다.");
                });
        verify(purchaseClickRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsUnsafeRedirectScheme() {
        BookPurchaseClickService service = createService();
        Book book = org.mockito.Mockito.mock(Book.class);
        given(book.getExternalUrl()).willReturn("javascript:alert(1)");
        given(bookRepository.findById(5L)).willReturn(Optional.of(book));

        assertThatThrownBy(() -> service.recordClick(7L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(
                        ((BusinessException) exception).errorCode())
                        .isEqualTo(ErrorCode.UNPROCESSABLE_ENTITY));
        verify(purchaseClickRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private BookPurchaseClickService createService() {
        return new BookPurchaseClickService(bookRepository, purchaseClickRepository);
    }
}
