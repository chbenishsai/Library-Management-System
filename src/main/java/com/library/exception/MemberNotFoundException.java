package com.library.exception;

/**
 * Thrown when a lookup for a member by ID or email finds no matching record.
 */
public class MemberNotFoundException extends LibraryException {

    public MemberNotFoundException(String message) {
        super(message);
    }
}
