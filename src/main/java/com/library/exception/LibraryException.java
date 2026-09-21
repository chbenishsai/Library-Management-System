package com.library.exception;

/**
 * Base checked exception for all domain-specific errors in the Library
 * Management System. All custom exceptions extend this class so callers
 * can choose to catch broadly (LibraryException) or narrowly
 * (e.g. BookNotAvailableException) as needed.
 */
public class LibraryException extends Exception {

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
