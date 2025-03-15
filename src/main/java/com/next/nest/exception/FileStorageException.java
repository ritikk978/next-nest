package com.next.nest.exception;

/**
 * Exception thrown when file storage operations fail.
 * Used to wrap IOExceptions and other low-level exceptions with more
 * meaningful application-specific context.
 */
public class FileStorageException extends RuntimeException {

    /**
     * Constructs a new file storage exception with the specified detail message.
     *
     * @param message the detail message
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Constructs a new file storage exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}