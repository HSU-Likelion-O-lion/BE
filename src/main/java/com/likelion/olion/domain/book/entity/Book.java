package com.likelion.olion.domain.book.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "books", uniqueConstraints = {
        @UniqueConstraint(name = "uk_books_isbn13", columnNames = "isbn13"),
        @UniqueConstraint(
                name = "uk_books_provider_book_id",
                columnNames = {"provider", "provider_book_id"}
        ),
        @UniqueConstraint(name = "uk_books_external_url", columnNames = "external_url")
})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    private String title;
    private String author;
    private String coverImageUrl;
    private String publisher;
    private String description;
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;
    @Column(name = "summary_prompt_version", length = 30)
    private String summaryPromptVersion;
    @Enumerated(EnumType.STRING)
    @Column(name = "summary_status", length = 20)
    private BookSummaryStatus summaryStatus;
    @Column(name = "external_url")
    private String externalUrl;
    private String provider;
    @Column(name = "isbn13", length = 13)
    private String isbn13;
    @Column(name = "provider_book_id", length = 100)
    private String providerBookId;
    private String category;

    protected Book() {
    }

    public static Book fromExternal(
            String title,
            String author,
            String coverImageUrl,
            String publisher,
            String description,
            String externalUrl,
            String provider,
            String isbn13,
            String providerBookId,
            String category
    ) {
        Book book = new Book();
        book.title = title;
        book.author = author;
        book.coverImageUrl = coverImageUrl;
        book.publisher = publisher;
        book.description = description;
        book.keywords = category;
        book.summaryStatus = BookSummaryStatus.NOT_GENERATED;
        book.externalUrl = externalUrl;
        book.provider = provider;
        book.isbn13 = isbn13;
        book.providerBookId = providerBookId;
        book.category = category;
        return book;
    }

    public void updateAiSummary(String summary, String promptVersion, BookSummaryStatus status) {
        this.aiSummary = summary;
        this.summaryPromptVersion = promptVersion;
        this.summaryStatus = status;
    }

    public void updateExternalMetadata(
            String title,
            String author,
            String coverImageUrl,
            String publisher,
            String description,
            String externalUrl,
            String provider,
            String isbn13,
            String providerBookId,
            String category
    ) {
        this.title = prefer(title, this.title);
        this.author = prefer(author, this.author);
        this.coverImageUrl = prefer(coverImageUrl, this.coverImageUrl);
        this.publisher = prefer(publisher, this.publisher);
        this.description = prefer(description, this.description);
        this.keywords = prefer(category, this.keywords);
        this.isbn13 = prefer(isbn13, this.isbn13);
        this.category = prefer(category, this.category);
        if (this.provider == null || this.provider.isBlank()
                || isHigherPriorityProvider(provider, this.provider)) {
            this.provider = provider;
            this.externalUrl = prefer(externalUrl, this.externalUrl);
            this.providerBookId = prefer(providerBookId, this.providerBookId);
        }
    }

    private static boolean isHigherPriorityProvider(String candidate, String current) {
        return "ALADIN".equalsIgnoreCase(candidate) && "KAKAO".equalsIgnoreCase(current);
    }

    private static String prefer(String candidate, String current) {
        return candidate == null || candidate.isBlank() ? current : candidate;
    }

    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getPublisher() { return publisher; }
    public String getDescription() { return description; }
    public String getKeywords() { return keywords; }
    public String getAiSummary() { return aiSummary; }
    public String getSummaryPromptVersion() { return summaryPromptVersion; }
    public BookSummaryStatus getSummaryStatus() { return summaryStatus; }
    public String getExternalUrl() { return externalUrl; }
    public String getProvider() { return provider; }
    public String getIsbn13() { return isbn13; }
    public String getProviderBookId() { return providerBookId; }
    public String getCategory() { return category; }
}
