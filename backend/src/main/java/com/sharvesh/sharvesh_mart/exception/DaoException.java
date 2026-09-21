package com.sharvesh.sharvesh_mart.exception;

/**
 * Thrown when a database operation fails. Checked so callers must handle or
 * propagate persistence failures explicitly.
 */
public class DaoException extends Exception {

    /**
     * Creates a data access exception with a message.
     *
     * @param message the detail message
     */
    public DaoException(String message) {
        super(message);
    }

    /**
     * Creates a data access exception wrapping an underlying cause.
     *
     * @param message the detail message
     * @param cause   the underlying {@link Exception} cause
     */
    public DaoException(String message, Exception cause) {
        super(message, cause);
    }
}
