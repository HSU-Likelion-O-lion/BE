package com.likelion.olion.domain.reading.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartResponse;
import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReadingSessionServiceTest {
    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private Book book;

    @Test
    void startsSessionWithAllowedTargetMinutes() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        UserBook userBook = new UserBook(1L, book);
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L)).willReturn(Optional.of(userBook));
        given(readingSessionRepository.existsByUserIdAndStatus(1L, ReadingSessionStatus.IN_PROGRESS))
                .willReturn(false);
        given(readingSessionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        ReadingSessionStartResponse response = service.start(
                1L, new ReadingSessionStartRequest(10L, 30));

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(response.startedAt()).isNotNull();
    }

    @Test
    void rejectsUnsupportedTargetMinutes() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L))
                .willReturn(Optional.of(new UserBook(1L, book)));

        assertThatThrownBy(() -> service.start(1L, new ReadingSessionStartRequest(10L, 20)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsWhenAnotherSessionIsInProgress() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L))
                .willReturn(Optional.of(new UserBook(1L, book)));
        given(readingSessionRepository.existsByUserIdAndStatus(1L, ReadingSessionStatus.IN_PROGRESS))
                .willReturn(true);

        assertThatThrownBy(() -> service.start(1L, new ReadingSessionStartRequest(10L, 30)))
                .isInstanceOf(BusinessException.class);
    }
}
