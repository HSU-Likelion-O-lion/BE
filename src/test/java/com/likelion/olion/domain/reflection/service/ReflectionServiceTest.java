package com.likelion.olion.domain.reflection.service;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import com.likelion.olion.domain.reflection.dto.ReflectionCreateRequest;
import com.likelion.olion.domain.reflection.dto.ReflectionCreateResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionDeleteResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionListResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionPublishableResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionUpdateRequest;
import com.likelion.olion.domain.reflection.dto.ReflectionUpdateResponse;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReflectionServiceTest {
    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @Mock
    private CommunityPostRepository communityPostRepository;

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

    @Test
    void returnsReflectionListWithCoverProgress() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        Reflection reflection = new Reflection(1L, new ReadingSession(1L, mockUserBook(), 30), "오늘 읽은 부분에서...");
        given(reflectionRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(reflection));
        given(reflectionRepository.countByUserId(1L)).willReturn(3L);

        ReflectionListResponse response = service.getList(1L);

        assertThat(response.coverProgress()).isEqualTo(3);
        assertThat(response.reflections()).hasSize(1);
        assertThat(response.reflections().get(0).content()).isEqualTo("오늘 읽은 부분에서...");
    }

    @Test
    void returnsEmptyListWhenNoReflectionsYet() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        given(reflectionRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of());
        given(reflectionRepository.countByUserId(1L)).willReturn(0L);

        ReflectionListResponse response = service.getList(1L);

        assertThat(response.coverProgress()).isEqualTo(0);
        assertThat(response.reflections()).isEmpty();
    }

    @Test
    void updatesOwnedReflection() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        Reflection reflection = new Reflection(1L, new ReadingSession(1L, mockUserBook(), 30), "원래 내용");
        ReflectionTestUtils.setField(reflection, "reflectionId", 88L);
        given(reflectionRepository.findByReflectionIdAndUserId(88L, 1L)).willReturn(Optional.of(reflection));

        ReflectionUpdateResponse response = service.update(1L, 88L, new ReflectionUpdateRequest("수정된 내용"));

        assertThat(response.reflectionId()).isEqualTo(88L);
        assertThat(reflection.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    void synchronizesLinkedShelterPostWhenReflectionIsUpdated() {
        ReflectionService service = new ReflectionService(
                reflectionRepository, readingSessionRepository, communityPostRepository);
        Reflection reflection = new Reflection(1L, new ReadingSession(1L, mockUserBook(), 30), "원래 내용");
        ReflectionTestUtils.setField(reflection, "reflectionId", 88L);
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "원래 내용", 88L);
        given(reflectionRepository.findByReflectionIdAndUserId(88L, 1L)).willReturn(Optional.of(reflection));
        given(communityPostRepository.findByReflectionId(88L)).willReturn(List.of(post));

        service.update(1L, 88L, new ReflectionUpdateRequest("  수정된 내용  "));

        assertThat(reflection.getContent()).isEqualTo("수정된 내용");
        assertThat(post.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    void rejectsUpdateWhenReflectionNotOwnedOrMissing() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        given(reflectionRepository.findByReflectionIdAndUserId(88L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, 88L, new ReflectionUpdateRequest("수정된 내용")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deletesOwnedReflectionAndReturnsCoverProgress() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        Reflection reflection = new Reflection(1L, new ReadingSession(1L, mockUserBook(), 30), "삭제될 내용");
        ReflectionTestUtils.setField(reflection, "reflectionId", 88L);
        given(reflectionRepository.findByReflectionIdAndUserId(88L, 1L)).willReturn(Optional.of(reflection));
        given(reflectionRepository.countByUserId(1L)).willReturn(11L);

        ReflectionDeleteResponse response = service.delete(1L, 88L);

        assertThat(response.coverProgress()).isEqualTo(11);
        verify(reflectionRepository).delete(reflection);
    }

    @Test
    void deletesLinkedShelterPostsBeforeDeletingReflection() {
        ReflectionService service = new ReflectionService(
                reflectionRepository, readingSessionRepository, communityPostRepository);
        Reflection reflection = new Reflection(1L, new ReadingSession(1L, mockUserBook(), 30), "삭제될 내용");
        ReflectionTestUtils.setField(reflection, "reflectionId", 88L);
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "삭제될 내용", 88L);
        given(reflectionRepository.findByReflectionIdAndUserId(88L, 1L)).willReturn(Optional.of(reflection));
        given(communityPostRepository.findByReflectionId(88L)).willReturn(List.of(post));
        given(reflectionRepository.countByUserId(1L)).willReturn(11L);

        service.delete(1L, 88L);

        verify(communityPostRepository).deleteAll(List.of(post));
        verify(reflectionRepository).delete(reflection);
    }

    @Test
    void rejectsDeleteWhenReflectionNotOwnedOrMissing() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        given(reflectionRepository.findByReflectionIdAndUserId(88L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L, 88L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void returnsNotEnoughWhenBelowThreshold() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        given(reflectionRepository.countByUserId(1L)).willReturn(25L);

        ReflectionPublishableResponse response = service.getPublishable(1L);

        assertThat(response.canPublish()).isFalse();
        assertThat(response.needed()).isEqualTo(5);
        assertThat(response.reflections()).isEmpty();
    }

    @Test
    void returnsReadyWhenAtOrAboveThreshold() {
        ReflectionService service = new ReflectionService(reflectionRepository, readingSessionRepository);
        Reflection reflection = new Reflection(1L, new ReadingSession(1L, mockUserBook(), 30), "오늘 읽은 부분에서...");
        given(reflectionRepository.countByUserId(1L)).willReturn(30L);
        given(reflectionRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(reflection));

        ReflectionPublishableResponse response = service.getPublishable(1L);

        assertThat(response.canPublish()).isTrue();
        assertThat(response.needed()).isNull();
        assertThat(response.reflections()).hasSize(1);
    }

    private UserBook mockUserBook() {
        return mock(UserBook.class);
    }
}
