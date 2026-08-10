package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.community.dto.CommunityAccessResponse;
import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommunityAccessServiceTest {
    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @Mock
    private Book book;

    @Test
    void allowsEntryWhenUserCompletedSessionToday() {
        CommunityAccessService service = new CommunityAccessService(readingSessionRepository);
        ReadingSession session = new ReadingSession(1L, new UserBook(1L, book), 30);
        session.complete("Question");
        given(readingSessionRepository.findByUserIdAndStatus(1L, ReadingSessionStatus.COMPLETED))
                .willReturn(List.of(session));

        CommunityAccessResponse response = service.getAccess(1L);

        assertThat(response.canEnter()).isTrue();
    }

    @Test
    void deniesEntryWhenUserHasNoCompletedSessionToday() {
        CommunityAccessService service = new CommunityAccessService(readingSessionRepository);
        given(readingSessionRepository.findByUserIdAndStatus(1L, ReadingSessionStatus.COMPLETED))
                .willReturn(List.of());

        CommunityAccessResponse response = service.getAccess(1L);

        assertThat(response.canEnter()).isFalse();
    }
}
