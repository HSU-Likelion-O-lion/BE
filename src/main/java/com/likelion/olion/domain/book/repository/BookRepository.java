package com.likelion.olion.domain.book.repository;

import com.likelion.olion.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    Optional<Book> findByIsbn13(String isbn13);
    Optional<Book> findByProviderAndProviderBookId(String provider, String providerBookId);
    Optional<Book> findByExternalUrl(String externalUrl);
    Optional<Book> findFirstByTitleIgnoreCaseAndAuthorIgnoreCase(String title, String author);
}
