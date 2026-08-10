package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.dto.BookDetailResponse;
import com.likelion.olion.domain.book.dto.BookSearchResponse;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public BookDetailResponse getBook(Long bookId) {
        return bookRepository.findById(bookId)
                .map(BookDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public BookSearchResponse searchBooks(String query) {
        if (query == null || query.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "검색어를 입력해주세요.");
        }

        return BookSearchResponse.from(
                bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query.trim(), query.trim())
        );
    }
}
