package com.sharvesh.sharvesh_mart.exception;

/**
 * Thrown when registration collides with an existing account (HTTP 409).
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    /**
     * Creates a conflict exception for the offending email address.
     *
     * @param email the already-registered email address
     */
    public DuplicateEmailException(String email) {
        super("An account with this email address already exists.");
        this.email = email;
    }

    /**
     * Returns the email address that caused the conflict.
     *
     * @return the duplicate email
     */
    public String getEmail() {
        return email;
    }
}
