package com.likelion.olion.domain.book.repository;

import com.likelion.olion.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
