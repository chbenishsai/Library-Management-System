package com.library.exception;

/**
 * Thrown when a lookup for a book by ID, ISBN, or other criteria
 * finds no matching record.
 */
public class BookNotFoundException extends LibraryException {

    public BookNotFoundException(String message) {
        super(message);
    }
}
