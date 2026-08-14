package com.likelion.olion.domain.bookshelf.entity;

public enum BookStatus {
    BEFORE_READING,
    READING,
    DONE;

    public static BookStatus from(String value) {
        try {
            return value == null ? null : valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
