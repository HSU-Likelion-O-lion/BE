package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.entity.BookSummaryStatus;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.global.ai.AiTextGenerator;
import com.likelion.olion.global.ai.PromptTemplateLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class BookSummaryService {
    private static final String PROMPT_VERSION = "v1";
    private static final int MAX_SUMMARY_LENGTH = 1_500;

    private final BookRepository bookRepository;
    private final AiTextGenerator aiTextGenerator;
    private final PromptTemplateLoader promptTemplateLoader;

    public BookSummaryService(
            BookRepository bookRepository,
            AiTextGenerator aiTextGenerator,
            PromptTemplateLoader promptTemplateLoader
    ) {
        this.bookRepository = bookRepository;
        this.aiTextGenerator = aiTextGenerator;
        this.promptTemplateLoader = promptTemplateLoader;
    }

    @Transactional
    public String ensureSummary(Book book) {
        if (hasText(book.getAiSummary())) {
            return book.getAiSummary();
        }
        if (!hasText(book.getDescription())) {
            book.updateAiSummary(null, PROMPT_VERSION, BookSummaryStatus.NOT_GENERATED);
            return null;
        }

        String fallback = limit(book.getDescription(), MAX_SUMMARY_LENGTH);
        String prompt = promptTemplateLoader.load("book-summary", PROMPT_VERSION, Map.of(
                "title", valueOrEmpty(book.getTitle()),
                "author", valueOrEmpty(book.getAuthor()),
                "description", book.getDescription()));
        String summary = aiTextGenerator.generate(null, "book-summary", prompt, fallback);
        BookSummaryStatus status = summary.equals(fallback)
                ? BookSummaryStatus.FALLBACK
                : BookSummaryStatus.COMPLETED;
        book.updateAiSummary(limit(summary, MAX_SUMMARY_LENGTH), PROMPT_VERSION, status);
        bookRepository.save(book);
        return book.getAiSummary();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
