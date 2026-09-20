package com.saranya.saranyamart.exception;

/**
 * Thrown when user-supplied input fails validation. Carries the offending
 * field so a form or API client can render a field-level error (HTTP 400).
 */
public class ValidationException extends Exception {

    private final String field;

    /**
     * Creates a validation exception for a specific form field.
     *
     * @param field   the name of the field that failed validation
     * @param message a human-readable description of the problem
     */
    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    /**
     * Returns the name of the field that failed validation.
     *
     * @return the field name
     */
    public String getField() {
        return field;
    }
}
