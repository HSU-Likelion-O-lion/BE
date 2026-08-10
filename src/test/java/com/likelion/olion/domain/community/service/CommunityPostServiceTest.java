package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.community.dto.CommunityPostPreviewResponse;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommunityPostServiceTest {
    @Mock
    private CommunityAccessChecker communityAccessChecker;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private Book book;

    @Test
    void returnsFirstLinePreviews() {
        CommunityPostService service = new CommunityPostService(
                communityAccessChecker, userBookRepository, communityPostRepository);
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
                communityAccessChecker, userBookRepository, communityPostRepository);
        given(communityAccessChecker.canEnter(1L)).willReturn(false);

        assertThatThrownBy(() -> service.getPreviews(1L, 12L))
                .isInstanceOf(BusinessException.class);
    }
}
