package com.library.exception;

/**
 * Thrown by the repository layer when a JDBC / SQL operation fails
 * unexpectedly. This wraps the underlying {@link java.sql.SQLException}
 * so that the service and UI layers do not need to depend on
 * java.sql directly.
 */
public class DatabaseOperationException extends LibraryException {

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
