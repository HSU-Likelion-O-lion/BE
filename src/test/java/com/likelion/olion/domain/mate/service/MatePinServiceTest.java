package com.likelion.olion.domain.mate.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.mate.dto.MatePinRequest;
import com.likelion.olion.domain.mate.dto.MatePinSaveResponse;
import com.likelion.olion.domain.mate.entity.MatePin;
import com.likelion.olion.domain.mate.repository.MatePinRepository;
import com.likelion.olion.domain.user.entity.SubscriptionPlan;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class MatePinServiceTest {
    @Mock
    private MatePinRepository matePinRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Book book;

    private User userWithPlan(SubscriptionPlan plan) {
        User user = User.builder().email("test@example.com").password("encoded").nickname("닉네임").build();
        ReflectionTestUtils.setField(user, "plan", plan);
        return user;
    }

    @Test
    void addsPinWithFirstAvailableOrder() {
        MatePinService service = new MatePinService(matePinRepository, userBookRepository, userRepository);
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
        MatePinService service = new MatePinService(matePinRepository, userBookRepository, userRepository);
        UserBook userBook = new UserBook(1L, book);
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L)).willReturn(Optional.of(userBook));
        given(matePinRepository.existsByUserIdAndUserBookUserBookId(1L, 10L)).willReturn(false);
        given(matePinRepository.countByUserId(1L)).willReturn(5L);

        assertThatThrownBy(() -> service.addPin(1L, new MatePinRequest(10L)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsPinForAnotherUsersBook() {
        MatePinService service = new MatePinService(matePinRepository, userBookRepository, userRepository);
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.addPin(1L, new MatePinRequest(10L)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void basicPlanRejectsPinWhenThreeBooksAreAlreadyPinned() {
        MatePinService service = new MatePinService(matePinRepository, userBookRepository, userRepository);
        UserBook userBook = new UserBook(1L, book);
        given(userRepository.findById(1L)).willReturn(Optional.of(userWithPlan(SubscriptionPlan.BASIC)));
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L)).willReturn(Optional.of(userBook));
        given(matePinRepository.existsByUserIdAndUserBookUserBookId(1L, 10L)).willReturn(false);
        given(matePinRepository.countByUserId(1L)).willReturn(3L);

        assertThatThrownBy(() -> service.addPin(1L, new MatePinRequest(10L)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void proPlanAllowsPinningUpToSevenBooks() {
        MatePinService service = new MatePinService(matePinRepository, userBookRepository, userRepository);
        UserBook userBook = new UserBook(1L, book);
        given(userRepository.findById(1L)).willReturn(Optional.of(userWithPlan(SubscriptionPlan.PRO)));
        given(userBookRepository.findByUserBookIdAndUserId(10L, 1L)).willReturn(Optional.of(userBook));
        given(matePinRepository.existsByUserIdAndUserBookUserBookId(1L, 10L)).willReturn(false);
        given(matePinRepository.countByUserId(1L)).willReturn(6L);
        given(matePinRepository.findByUserIdOrderByPinnedOrderAsc(1L)).willReturn(List.of());

        MatePinSaveResponse response = service.addPin(1L, new MatePinRequest(10L));

        assertThat(response.pinnedOrder()).isEqualTo(1);
    }
}
