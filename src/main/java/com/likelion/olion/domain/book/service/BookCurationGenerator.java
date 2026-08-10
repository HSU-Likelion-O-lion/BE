package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.entity.Book;

import java.util.List;

public interface BookCurationGenerator {
    String generate(Book book, List<Integer> likedCardIds);
}
