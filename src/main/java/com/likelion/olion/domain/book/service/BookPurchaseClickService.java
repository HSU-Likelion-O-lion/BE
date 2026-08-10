package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.dto.BookPurchaseClickResponse;
import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.entity.BookPurchaseClick;
import com.likelion.olion.domain.book.repository.BookPurchaseClickRepository;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
public class BookPurchaseClickService {
    private static final String PURCHASE_LINK_UNAVAILABLE_MESSAGE =
            "구매 링크를 제공하지 않는 도서입니다.";

    private final BookRepository bookRepository;
    private final BookPurchaseClickRepository purchaseClickRepository;

    public BookPurchaseClickService(
            BookRepository bookRepository,
            BookPurchaseClickRepository purchaseClickRepository
    ) {
        this.bookRepository = bookRepository;
        this.purchaseClickRepository = purchaseClickRepository;
    }

    @Transactional
    public BookPurchaseClickResponse recordClick(Long userId, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));
        String redirectUrl = validateAndNormalizeRedirectUrl(book.getExternalUrl());

        purchaseClickRepository.save(new BookPurchaseClick(book, userId, redirectUrl));
        return new BookPurchaseClickResponse(redirectUrl);
    }

    private String validateAndNormalizeRedirectUrl(String externalUrl) {
        if (externalUrl == null || externalUrl.isBlank()) {
            throw purchaseLinkUnavailableException();
        }

        String normalizedUrl = externalUrl.trim();
        URI uri;
        try {
            uri = URI.create(normalizedUrl);
        } catch (IllegalArgumentException exception) {
            throw purchaseLinkUnavailableException();
        }
        boolean supportedScheme = "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());
        if (!supportedScheme || uri.getHost() == null) {
            throw purchaseLinkUnavailableException();
        }
        return normalizedUrl;
    }

    private BusinessException purchaseLinkUnavailableException() {
        return new BusinessException(
                ErrorCode.UNPROCESSABLE_ENTITY, PURCHASE_LINK_UNAVAILABLE_MESSAGE);
    }
}
