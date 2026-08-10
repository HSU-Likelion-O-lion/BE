package com.likelion.olion.domain.book.client;

import java.util.List;

public interface BookSearchProvider {
    List<BookSearchResult> search(String query);
}
