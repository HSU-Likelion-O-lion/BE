package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.client.BookSearchResult;
import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ExternalBookWriter {
    private final BookRepository bookRepository;

    public ExternalBookWriter(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Book saveOrUpdate(BookSearchResult result) {
        Book book = findExisting(result).orElseGet(() -> Book.fromExternal(
                result.title(),
                result.author(),
                result.coverImageUrl(),
                result.publisher(),
                result.description(),
                result.externalUrl(),
                result.provider(),
                result.isbn13(),
                result.providerBookId(),
                result.category()
        ));
        book.updateExternalMetadata(
                result.title(),
                result.author(),
                result.coverImageUrl(),
                result.publisher(),
                result.description(),
                result.externalUrl(),
                result.provider(),
                result.isbn13(),
                result.providerBookId(),
                result.category()
        );
        return bookRepository.saveAndFlush(book);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<Book> findExisting(BookSearchResult result) {
        if (hasText(result.isbn13())) {
            Optional<Book> byIsbn = bookRepository.findByIsbn13(result.isbn13());
            if (byIsbn.isPresent()) {
                return byIsbn;
            }
        }
        if (hasText(result.provider()) && hasText(result.providerBookId())) {
            Optional<Book> byProviderId = bookRepository.findByProviderAndProviderBookId(
                    result.provider(), result.providerBookId());
            if (byProviderId.isPresent()) {
                return byProviderId;
            }
        }
        if (hasText(result.externalUrl())) {
            Optional<Book> byExternalUrl = bookRepository.findByExternalUrl(result.externalUrl());
            if (byExternalUrl.isPresent()) {
                return byExternalUrl;
            }
        }
        if (hasText(result.title()) && hasText(result.author())) {
            return bookRepository.findFirstByTitleIgnoreCaseAndAuthorIgnoreCase(
                    normalizeText(result.title()), normalizeText(result.author()));
        }
        return Optional.empty();
    }

    private String normalizeText(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
