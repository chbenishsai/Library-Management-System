package com.library.exception;

/**
 * Thrown when an attempt is made to issue a book that currently has
 * zero available copies.
 */
public class BookNotAvailableException extends LibraryException {

    public BookNotAvailableException(String message) {
        super(message);
    }
}
