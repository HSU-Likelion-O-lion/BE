package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.dto.BookDetailResponse;
import com.likelion.olion.domain.book.dto.BookSearchResponse;
import com.likelion.olion.domain.book.client.BookSearchProvider;
import com.likelion.olion.domain.book.client.BookSearchResult;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.logging.Logger;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final List<BookSearchProvider> searchProviders;
    private final ExternalBookSyncService externalBookSyncService;
    private final BookSummaryService bookSummaryService;
    private static final Logger log = Logger.getLogger(BookService.class.getName());

    public BookService(BookRepository bookRepository) {
        this(bookRepository, List.of(), null, null);
    }

    @Autowired
    public BookService(
            BookRepository bookRepository,
            List<BookSearchProvider> searchProviders,
            ExternalBookSyncService externalBookSyncService,
            BookSummaryService bookSummaryService
    ) {
        this.bookRepository = bookRepository;
        this.searchProviders = searchProviders;
        this.externalBookSyncService = externalBookSyncService;
        this.bookSummaryService = bookSummaryService;
    }

    public BookService(
            BookRepository bookRepository,
            List<BookSearchProvider> searchProviders,
            ExternalBookSyncService externalBookSyncService
    ) {
        this(bookRepository, searchProviders, externalBookSyncService, null);
    }

    @Transactional
    public BookDetailResponse getBook(Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> {
                    if (bookSummaryService != null) {
                        bookSummaryService.ensureSummary(book);
                    }
                    return BookDetailResponse.from(book);
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));
    }

    public BookSearchResponse searchBooks(String query) {
        if (query == null || query.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "검색어를 입력해주세요.");
        }

        String normalizedQuery = query.trim();
        if (searchProviders.isEmpty()) {
            return BookSearchResponse.from(bookRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(normalizedQuery, normalizedQuery));
        }

        BookSearchProvider primaryProvider = searchProviders.getFirst();
        try {
            List<BookSearchResult> primaryResults = primaryProvider.search(normalizedQuery);
            if (!primaryResults.isEmpty()) {
                List<BookSearchResult> mergedResults = primaryResults;
                for (BookSearchProvider supplementProvider : searchProviders.subList(1, searchProviders.size())) {
                    if (mergedResults.stream().noneMatch(BookSearchResult::hasMissingMetadata)) {
                        break;
                    }
                    try {
                        mergedResults = BookMetadataResolver.merge(
                                mergedResults, supplementProvider.search(normalizedQuery));
                    } catch (RuntimeException exception) {
                        log.warning("Book supplement provider failed: " + exception.getMessage());
                    }
                }
                return BookSearchResponse.from(externalBookSyncService.synchronize(mergedResults));
            }
        } catch (RuntimeException exception) {
            log.warning("Book primary provider search failed: " + exception.getMessage());
        }

        for (BookSearchProvider provider : searchProviders.subList(1, searchProviders.size())) {
            try {
                List<BookSearchResult> results = provider.search(normalizedQuery);
                if (!results.isEmpty()) {
                    return BookSearchResponse.from(externalBookSyncService.synchronize(results));
                }
            } catch (RuntimeException exception) {
                log.warning("Book provider search failed: " + exception.getMessage());
            }
        }

        return BookSearchResponse.from(bookRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(normalizedQuery, normalizedQuery));
    }
}
