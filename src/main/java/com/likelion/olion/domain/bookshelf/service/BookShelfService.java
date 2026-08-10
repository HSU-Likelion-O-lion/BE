package com.likelion.olion.domain.bookshelf.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.domain.bookshelf.dto.BookShelfRequest;
import com.likelion.olion.domain.bookshelf.dto.BookShelfResponse;
import com.likelion.olion.domain.bookshelf.dto.BookShelfSaveResponse;
import com.likelion.olion.domain.bookshelf.entity.BookStatus;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookShelfService {
    private final UserBookRepository userBookRepository;
    private final BookRepository bookRepository;

    public BookShelfService(UserBookRepository userBookRepository, BookRepository bookRepository) {
        this.userBookRepository = userBookRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public BookShelfResponse getBookshelf(Long userId, String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return BookShelfResponse.from(userBookRepository.findByUserIdOrderByCreatedAtDesc(userId));
        }

        BookStatus status = parseStatus(statusValue);
        return BookShelfResponse.from(userBookRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status));
    }

    @Transactional
    public BookShelfSaveResponse addBook(Long userId, BookShelfRequest request) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));
        if (userBookRepository.existsByUserIdAndBookBookId(userId, book.getBookId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 책장에 담긴 도서입니다.");
        }

        UserBook userBook = userBookRepository.save(new UserBook(userId, book));
        return new BookShelfSaveResponse(userBook.getUserBookId(), userBook.getStatus().name());
    }

    @Transactional
    public BookShelfSaveResponse changeStatus(Long userId, Long userBookId, String statusValue) {
        BookStatus status = parseStatus(statusValue);
        UserBook userBook = getOwnedBook(userId, userBookId);
        userBook.changeStatus(status);
        return new BookShelfSaveResponse(userBook.getUserBookId(), userBook.getStatus().name());
    }

    @Transactional
    public void deleteBook(Long userId, Long userBookId) {
        UserBook userBook = getOwnedBook(userId, userBookId);
        userBookRepository.delete(userBook);
    }

    private UserBook getOwnedBook(Long userId, Long userBookId) {
        return userBookRepository.findByUserBookIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "책장 항목을 찾을 수 없습니다."));
    }

    private BookStatus parseStatus(String value) {
        BookStatus status = BookStatus.from(value);
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "유효하지 않은 status 값입니다.");
        }
        return status;
    }
}
