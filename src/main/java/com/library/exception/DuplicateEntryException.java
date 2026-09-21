package com.library.exception;

/**
 * Thrown when attempting to insert a record that violates a uniqueness
 * constraint (e.g. duplicate ISBN or duplicate member email).
 */
public class DuplicateEntryException extends LibraryException {

    public DuplicateEntryException(String message) {
        super(message);
    }
}
