package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.community.dto.CommunityAccessResponse;
import com.likelion.olion.domain.community.dto.CommunityRoomResponse;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommunityRoomServiceTest {
    @Mock
    private CommunityAccessService communityAccessService;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private Book book;

    @Test
    void returnsRoomsFromUsersBookshelf() {
        CommunityRoomService service = new CommunityRoomService(communityAccessService, userBookRepository);
        UserBook userBook = new UserBook(1L, book);
        given(communityAccessService.getAccess(1L)).willReturn(new CommunityAccessResponse(true));
        given(userBookRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(userBook));
        given(book.getBookId()).willReturn(5L);
        given(book.getTitle()).willReturn("아몬드");

        CommunityRoomResponse response = service.getRooms(1L);

        assertThat(response.rooms()).hasSize(1);
        assertThat(response.rooms().get(0).roomId()).isEqualTo(userBook.getUserBookId());
        assertThat(response.rooms().get(0).bookTitle()).isEqualTo("아몬드");
    }

    @Test
    void deniesRoomsWhenUserCannotEnterCommunity() {
        CommunityRoomService service = new CommunityRoomService(communityAccessService, userBookRepository);
        given(communityAccessService.getAccess(1L)).willReturn(new CommunityAccessResponse(false));

        assertThatThrownBy(() -> service.getRooms(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getRoomsRunsInsideTransactionSoLazyBookCanBeLoaded() throws NoSuchMethodException {
        Method getRooms = CommunityRoomService.class.getMethod("getRooms", Long.class);

        assertThat(getRooms.isAnnotationPresent(Transactional.class))
                .as("getRooms must run inside a transaction so the lazily-loaded UserBook.book can be accessed "
                        + "after the repository call returns (open-in-view is disabled)")
                .isTrue();
    }
}
