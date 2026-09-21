package com.library.exception;

/**
 * Thrown when a lookup for an issue/return transaction finds no matching
 * active record (e.g. trying to return a book that isn't currently issued
 * to the given member).
 */
public class TransactionNotFoundException extends LibraryException {

    public TransactionNotFoundException(String message) {
        super(message);
    }
}
