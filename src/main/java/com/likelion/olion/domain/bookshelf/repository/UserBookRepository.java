package com.likelion.olion.domain.bookshelf.repository;

import com.likelion.olion.domain.bookshelf.entity.BookStatus;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    List<UserBook> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserBook> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, BookStatus status);

    Optional<UserBook> findByUserBookIdAndUserId(Long userBookId, Long userId);

    boolean existsByUserIdAndBookBookId(Long userId, Long bookId);
}
