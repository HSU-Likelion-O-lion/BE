package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.community.dto.CommunityRoomResponse;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class CommunityRoomService {
    private final CommunityAccessService communityAccessService;
    private final UserBookRepository userBookRepository;

    public CommunityRoomService(
            CommunityAccessService communityAccessService,
            UserBookRepository userBookRepository
    ) {
        this.communityAccessService = communityAccessService;
        this.userBookRepository = userBookRepository;
    }

    public CommunityRoomResponse getRooms(Long userId) {
        if (!communityAccessService.getAccess(userId).canEnter()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return new CommunityRoomResponse(userBookRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toRoom)
                .toList());
    }

    private CommunityRoomResponse.Room toRoom(UserBook userBook) {
        Book book = userBook.getBook();
        return new CommunityRoomResponse.Room(
                userBook.getUserBookId(),
                book.getBookId(),
                book.getTitle());
    }
}
