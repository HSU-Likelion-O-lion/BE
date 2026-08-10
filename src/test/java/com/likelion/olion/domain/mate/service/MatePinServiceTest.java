package com.likelion.olion.domain.mate.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.mate.dto.MatePinRequest;
import com.likelion.olion.domain.mate.dto.MatePinSaveResponse;
import com.likelion.olion.domain.mate.entity.MatePin;
import com.likelion.olion.domain.mate.repository.MatePinRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MatePinServiceTest {
    @Mock
    private MatePinRepository matePinRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private Book book;

    @Test
    void addsPinWithFirstAvailableOrder() {
        MatePinService service = new MatePinService(matePinRepository, userBookRepository);
        UserBook userBook = new UserBook(1L, book);
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L)).willReturn(Optional.of(userBook));
        given(matePinRepository.existsByUserIdAndUserBookUserBookId(1L, 10L)).willReturn(false);
        given(matePinRepository.countByUserId(1L)).willReturn(0L);
        given(matePinRepository.findByUserIdOrderByPinnedOrderAsc(1L)).willReturn(List.of());

        MatePinSaveResponse response = service.addPin(1L, new MatePinRequest(10L));

        assertThat(response.pinnedOrder()).isEqualTo(1);
    }

    @Test
    void rejectsPinWhenFiveBooksAreAlreadyPinned() {
        MatePinService service = new MatePinService(matePinRepository, userBookRepository);
        UserBook userBook = new UserBook(1L, book);
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L)).willReturn(Optional.of(userBook));
        given(matePinRepository.existsByUserIdAndUserBookUserBookId(1L, 10L)).willReturn(false);
        given(matePinRepository.countByUserId(1L)).willReturn(5L);

        assertThatThrownBy(() -> service.addPin(1L, new MatePinRequest(10L)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsPinForAnotherUsersBook() {
        MatePinService service = new MatePinService(matePinRepository, userBookRepository);
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.addPin(1L, new MatePinRequest(10L)))
                .isInstanceOf(BusinessException.class);
    }
}
