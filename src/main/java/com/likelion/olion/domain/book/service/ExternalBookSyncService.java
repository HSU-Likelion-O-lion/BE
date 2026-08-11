package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.client.BookSearchResult;
import com.likelion.olion.domain.book.entity.Book;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalBookSyncService {
    private final ExternalBookWriter externalBookWriter;

    public ExternalBookSyncService(ExternalBookWriter externalBookWriter) {
        this.externalBookWriter = externalBookWriter;
    }

    public List<Book> synchronize(List<BookSearchResult> results) {
        List<Book> books = new ArrayList<>();
        for (BookSearchResult result : results) {
            try {
                books.add(externalBookWriter.saveOrUpdate(result));
            } catch (DataIntegrityViolationException exception) {
                books.add(externalBookWriter.findExisting(result).orElseThrow(() -> exception));
            }
        }
        return books;
    }
}
