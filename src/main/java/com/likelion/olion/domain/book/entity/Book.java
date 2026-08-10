package com.likelion.olion.domain.book.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    private String title;
    private String author;
    private String coverImageUrl;
    private String publisher;
    private String description;
    private String externalUrl;
    private String provider;

    protected Book() {
    }

    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getPublisher() { return publisher; }
    public String getDescription() { return description; }
    public String getExternalUrl() { return externalUrl; }
    public String getProvider() { return provider; }
}
