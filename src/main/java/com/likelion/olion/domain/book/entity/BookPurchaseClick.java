package com.likelion.olion.domain.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "book_purchase_clicks")
public class BookPurchaseClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clickId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 1000)
    private String redirectUrl;

    @Column(nullable = false)
    private Instant clickedAt;

    protected BookPurchaseClick() {
    }

    public BookPurchaseClick(Book book, Long userId, String redirectUrl) {
        this.book = book;
        this.userId = userId;
        this.redirectUrl = redirectUrl;
        this.clickedAt = Instant.now();
    }

    public Long getClickId() { return clickId; }
    public Book getBook() { return book; }
    public Long getUserId() { return userId; }
    public String getRedirectUrl() { return redirectUrl; }
    public Instant getClickedAt() { return clickedAt; }
}
