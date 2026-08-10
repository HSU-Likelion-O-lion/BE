package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.community.dto.CommunityPostCreateRequest;
import com.likelion.olion.domain.community.dto.CommunityPostCreateResponse;
import com.likelion.olion.domain.community.dto.CommunityPostPreviewResponse;
import com.likelion.olion.domain.community.dto.CommunityPostListResponse;
import com.likelion.olion.domain.community.dto.CommunityPostUpdateRequest;
import com.likelion.olion.domain.community.dto.CommunityPostUpdateResponse;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommunityPostServiceTest {
    @Mock
    private CommunityAccessChecker communityAccessChecker;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityPostPolicy communityPostPolicy;

    @Mock
    private Book book;

    @Test
    void returnsFirstLinePreviews() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        given(communityAccessChecker.canEnter(1L)).willReturn(true);
        given(userBookRepository.findByUserBookIdAndUserId(12L, 1L))
                .willReturn(Optional.of(new UserBook(1L, book)));
        given(communityPostRepository.findByRoomIdOrderByCreatedAtDesc(12L)).willReturn(List.of(
                new CommunityPost(12L, 2L, "고요한 파도", "오늘따라 유독 마음이...\n두 번째 줄")));

        CommunityPostPreviewResponse response = service.getPreviews(1L, 12L);

        assertThat(response.previews()).hasSize(1);
        assertThat(response.previews().get(0).firstLine()).isEqualTo("오늘따라 유독 마음이...");
    }

    @Test
    void deniesPreviewWhenUserCannotEnter() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        given(communityAccessChecker.canEnter(1L)).willReturn(false);

        assertThatThrownBy(() -> service.getPreviews(1L, 12L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void returnsPostsWithMineAndHeartDefaults() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        given(communityAccessChecker.canEnter(1L)).willReturn(true);
        given(userBookRepository.findByUserBookIdAndUserId(12L, 1L))
                .willReturn(Optional.of(new UserBook(1L, book)));
        given(communityPostRepository.findByRoomIdOrderByCreatedAtDesc(12L)).willReturn(List.of(
                new CommunityPost(12L, 1L, "고요한 파도", "내 글"),
                new CommunityPost(12L, 2L, "조용한 새벽", "다른 사람의 글")));

        CommunityPostListResponse response = service.getPosts(1L, 12L);

        assertThat(response.posts()).hasSize(2);
        assertThat(response.posts().get(0).isMine()).isTrue();
        assertThat(response.posts().get(0).heartCount()).isZero();
        assertThat(response.posts().get(1).isMine()).isFalse();
        assertThat(response.posts().get(1).heartCount()).isNull();
        assertThat(response.posts().get(1).isHearted()).isFalse();
    }

    @Test
    void createsAnonymousPost() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        given(communityAccessChecker.canEnter(1L)).willReturn(true);
        given(userBookRepository.findByUserBookIdAndUserId(12L, 1L))
                .willReturn(Optional.of(new UserBook(1L, book)));
        given(communityPostRepository.countByUserIdAndCreatedAtAfter(any(), any(Instant.class)))
                .willReturn(0L);
        given(communityPostPolicy.createAnonymousNickname(1L, 12L)).willReturn("고요한 파도");
        given(communityPostRepository.save(any(CommunityPost.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        CommunityPostCreateResponse response = service.createPost(
                1L, new CommunityPostCreateRequest(12L, "  오늘의 사유  ", 31L));

        assertThat(response.anonymousNickname()).isEqualTo("고요한 파도");
        ArgumentCaptor<CommunityPost> postCaptor = ArgumentCaptor.forClass(CommunityPost.class);
        verify(communityPostRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getContent()).isEqualTo("오늘의 사유");
        assertThat(postCaptor.getValue().getReflectionId()).isEqualTo(31L);
    }

    @Test
    void rejectsBlankPostContent() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);

        assertThatThrownBy(() -> service.createPost(
                1L, new CommunityPostCreateRequest(12L, "  ", null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void rejectsProhibitedPostContent() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        given(communityPostPolicy.containsProhibitedWord("금칙어가 포함된 내용")).willReturn(true);

        assertThatThrownBy(() -> service.createPost(
                1L, new CommunityPostCreateRequest(12L, "금칙어가 포함된 내용", null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.UNPROCESSABLE_ENTITY);
    }

    @Test
    void rejectsSixthPostWithinThreeMinutes() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        given(communityAccessChecker.canEnter(1L)).willReturn(true);
        given(userBookRepository.findByUserBookIdAndUserId(12L, 1L))
                .willReturn(Optional.of(new UserBook(1L, book)));
        given(communityPostRepository.countByUserIdAndCreatedAtAfter(any(), any(Instant.class)))
                .willReturn(5L);

        assertThatThrownBy(() -> service.createPost(
                1L, new CommunityPostCreateRequest(12L, "오늘의 사유", null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.TOO_MANY_REQUESTS);
    }

    @Test
    void updatesOwnPostContent() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "수정 전 내용");
        ReflectionTestUtils.setField(post, "postId", 200L);
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));

        CommunityPostUpdateResponse response = service.updatePost(
                1L, 200L, new CommunityPostUpdateRequest("  수정된 내용  "));

        assertThat(response.postId()).isEqualTo(200L);
        assertThat(post.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    void rejectsUpdateWhenPostDoesNotExist() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        given(communityPostRepository.findById(200L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePost(
                1L, 200L, new CommunityPostUpdateRequest("수정된 내용")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void rejectsUpdateByAnotherUser() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "수정 전 내용");
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> service.updatePost(
                1L, 200L, new CommunityPostUpdateRequest("수정된 내용")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void rejectsUpdateWithProhibitedContent() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "수정 전 내용");
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));
        given(communityPostPolicy.containsProhibitedWord("금칙어가 포함된 내용")).willReturn(true);

        assertThatThrownBy(() -> service.updatePost(
                1L, 200L, new CommunityPostUpdateRequest("금칙어가 포함된 내용")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.UNPROCESSABLE_ENTITY);
    }

    @Test
    void rejectsUpdateWithBlankContent() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "수정 전 내용");
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> service.updatePost(
                1L, 200L, new CommunityPostUpdateRequest("  ")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void deletesOwnPost() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "삭제할 내용");
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));

        service.deletePost(1L, 200L);

        verify(communityPostRepository).delete(post);
    }

    @Test
    void rejectsDeleteWhenPostDoesNotExist() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        given(communityPostRepository.findById(200L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePost(1L, 200L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void rejectsDeleteByAnotherUser() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository, communityPostPolicy);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "삭제할 내용");
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> service.deletePost(1L, 200L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }
}
