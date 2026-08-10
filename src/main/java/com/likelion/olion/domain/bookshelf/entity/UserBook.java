package com.likelion.olion.domain.bookshelf.entity;

import com.likelion.olion.domain.book.entity.Book;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;

import java.time.Instant;

@Entity
@Table(name = "user_books", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_book_user_book", columnNames = {"user_id", "book_id"})
})
public class UserBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userBookId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    private Instant createdAt;

    protected UserBook() {
    }

    public UserBook(Long userId, Book book) {
        this.userId = userId;
        this.book = book;
        this.status = BookStatus.BEFORE_READING;
        this.createdAt = Instant.now();
    }

    public Long getUserBookId() { return userBookId; }
    public Long getUserId() { return userId; }
    public Book getBook() { return book; }
    public BookStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void changeStatus(BookStatus status) {
        this.status = status;
    }
}
