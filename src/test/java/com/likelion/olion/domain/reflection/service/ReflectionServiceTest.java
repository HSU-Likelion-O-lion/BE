package com.likelion.olion.domain.reflection.service;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import com.likelion.olion.domain.reflection.dto.ReflectionCreateRequest;
import com.likelion.olion.domain.reflection.dto.ReflectionCreateResponse;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReflectionServiceTest {
    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @Test
    void createsReflectionForOwnedSession() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        ReadingSession session = new ReadingSession(1L, mockUserBook(), 30);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L)).willReturn(Optional.of(session));
        given(reflectionRepository.save(any(Reflection.class))).willAnswer(invocation -> {
            Reflection reflection = invocation.getArgument(0);
            ReflectionTestUtils.setField(reflection, "reflectionId", 88L);
            return reflection;
        });
        given(reflectionRepository.countByUserId(1L)).willReturn(5L);

        ReflectionCreateResponse response = service.create(1L, new ReflectionCreateRequest(100L, "오늘 읽은 부분에서..."));

        assertThat(response.reflectionId()).isEqualTo(88L);
        assertThat(response.coverProgress()).isEqualTo(5);
    }

    @Test
    void rejectsWhenSessionNotOwnedOrMissing() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        given(readingSessionRepository.findBySessionIdAndUserId(100L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(1L, new ReflectionCreateRequest(100L, "내용")))
                .isInstanceOf(BusinessException.class);
    }

    private UserBook mockUserBook() {
        return mock(UserBook.class);
    }
}
