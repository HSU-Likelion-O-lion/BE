package com.likelion.olion.domain.reading.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartResponse;
import com.likelion.olion.domain.reading.dto.ActiveReadingSessionResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionHeartbeatRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionHeartbeatResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionResumeResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionCompleteResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionAbandonResponse;
import com.likelion.olion.domain.reading.dto.ReadingInterruptionRequest;
import com.likelion.olion.domain.reading.dto.ReadingInterruptionResponse;
import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import com.likelion.olion.domain.reading.entity.ReadingInterruption;
import com.likelion.olion.domain.reading.entity.ReadingInterruptionReason;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import com.likelion.olion.domain.reading.repository.ReadingInterruptionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReadingSessionServiceTest {
    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @Mock
    private ReadingInterruptionRepository readingInterruptionRepository;

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

    @Test
    void returnsActiveSessionWithRemainingSeconds() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        given(readingSessionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(
                1L, ReadingSessionStatus.IN_PROGRESS)).willReturn(Optional.of(session));

        ActiveReadingSessionResponse response = service.getActive(1L);

        assertThat(response.session()).isNotNull();
        assertThat(response.session().status()).isEqualTo("IN_PROGRESS");
        assertThat(response.session().remainingSeconds()).isPositive();
    }

    @Test
    void returnsNullSessionWhenNothingIsInProgress() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        given(readingSessionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(
                1L, ReadingSessionStatus.IN_PROGRESS)).willReturn(Optional.empty());

        ActiveReadingSessionResponse response = service.getActive(1L);

        assertThat(response.session()).isNull();
    }

    @Test
    void returnsServerRemainingTimeAndValidHeartbeat() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(session));

        ReadingSessionHeartbeatResponse response = service.heartbeat(
                1L, 100L, new ReadingSessionHeartbeatRequest(0));

        assertThat(response.remainingSeconds()).isPositive();
        assertThat(response.valid()).isTrue();
    }

    @Test
    void marksHeartbeatInvalidWhenClientTimeDiffersTooMuch() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(session));

        ReadingSessionHeartbeatResponse response = service.heartbeat(
                1L, 100L, new ReadingSessionHeartbeatRequest(120));

        assertThat(response.valid()).isFalse();
    }

    @Test
    void resumesInProgressSessionWithRemainingSeconds() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(session));

        ReadingSessionResumeResponse response = service.resume(1L, 100L);

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(response.remainingSeconds()).isPositive();
    }

    @Test
    void rejectsResumeWhenSessionDoesNotExist() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.resume(1L, 100L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void completesInProgressSessionAndReturnsAiQuestion() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(session));

        ReadingSessionCompleteResponse response = service.complete(1L, 100L);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.aiQuestion()).isNotBlank();
        assertThat(session.getStatus()).isEqualTo(ReadingSessionStatus.COMPLETED);
    }

    @Test
    void rejectsCompletingAlreadyFinishedSession() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        session.complete("Already completed");
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(session));

        assertThatThrownBy(() -> service.complete(1L, 100L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void abandonsInProgressSession() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(session));

        ReadingSessionAbandonResponse response = service.abandon(1L, 100L);

        assertThat(response.status()).isEqualTo("ABANDONED");
        assertThat(session.getStatus()).isEqualTo(ReadingSessionStatus.ABANDONED);
    }

    @Test
    void rejectsAbandoningAlreadyFinishedSession() {
        ReadingSessionService service = new ReadingSessionService(readingSessionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        session.complete("Already completed");
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(session));

        assertThatThrownBy(() -> service.abandon(1L, 100L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void recordsInterruptionReason() {
        ReadingSessionService service = new ReadingSessionService(
                readingSessionRepository, readingInterruptionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(session));
        given(readingInterruptionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        ReadingInterruptionResponse response = service.recordInterruption(
                1L, 100L, new ReadingInterruptionRequest(
                        "OTHER", "갑자기 졸려서요", java.time.Instant.now()));

        assertThat(response).isNotNull();
        verify(readingInterruptionRepository).save(any());
    }

    @Test
    void rejectsOtherReasonWithoutCustomText() {
        ReadingSessionService service = new ReadingSessionService(
                readingSessionRepository, readingInterruptionRepository, userBookRepository);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(new ReadingSession(1L, new UserBook(1L, book), 30)));

        assertThatThrownBy(() -> service.recordInterruption(
                1L, 100L, new ReadingInterruptionRequest(
                        "OTHER", "", java.time.Instant.now())))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deletesRecoverySessionOwnedByUser() {
        ReadingSessionService service = new ReadingSessionService(
                readingSessionRepository, readingInterruptionRepository, userBookRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.of(session));

        service.deleteRecoverySession(1L, 100L);

        verify(readingSessionRepository).delete(session);
    }

    @Test
    void rejectsDeletingMissingRecoverySession() {
        ReadingSessionService service = new ReadingSessionService(
                readingSessionRepository, readingInterruptionRepository, userBookRepository);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteRecoverySession(1L, 100L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void aggregatesReadingStatistics() {
        ReadingSessionService service = new ReadingSessionService(
                readingSessionRepository, readingInterruptionRepository, userBookRepository);
        ReadingSession completedSession = new ReadingSession(1L, new UserBook(1L, book), 30);
        completedSession.complete("Question");
        ReadingSession interruptionSession = new ReadingSession(1L, new UserBook(1L, book), 15);
        given(readingSessionRepository.findByUserIdAndStatus(1L, ReadingSessionStatus.COMPLETED))
                .willReturn(List.of(completedSession));
        given(readingInterruptionRepository.findBySessionUserId(1L)).willReturn(List.of(
                new ReadingInterruption(interruptionSession, ReadingInterruptionReason.CONTINUE,
                        null, java.time.Instant.now()),
                new ReadingInterruption(interruptionSession, ReadingInterruptionReason.EBOOK_SWITCH,
                        null, java.time.Instant.now())));

        var response = service.getStatistics(1L);

        assertThat(response.continueCount()).isEqualTo(1);
        assertThat(response.ebookSwitchCount()).isEqualTo(1);
        assertThat(response.byWeekday()).hasSize(1);
        assertThat(response.byHour()).hasSize(1);
        assertThat(response.byWeekday().get(0).focusedMinutes()).isEqualTo(30);
    }
}
