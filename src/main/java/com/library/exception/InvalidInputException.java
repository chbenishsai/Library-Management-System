package com.library.exception;

/**
 * Thrown when user-supplied input fails validation rules
 * (e.g. malformed email, phone number not 10 digits, blank required field).
 */
public class InvalidInputException extends LibraryException {

    public InvalidInputException(String message) {
        super(message);
    }
}
