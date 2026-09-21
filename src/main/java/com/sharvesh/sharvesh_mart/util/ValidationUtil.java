package com.sharvesh.sharvesh_mart.util;

import java.util.regex.Pattern;

import com.sharvesh.sharvesh_mart.exception.ValidationException;
import com.sharvesh.sharvesh_mart.model.Role;

/**
 * Field-level validation helpers. Validation runs at the top of every service
 * method, before any DAO call (spec Section 13 rule 5).
 */
public final class ValidationUtil {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int EMAIL_MAX_LENGTH = 255;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 64;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidationUtil() {
        // Utility class, not instantiable.
    }

    /**
     * Validates a registration form. Throws a field-level
     * {@link ValidationException} on the first invalid input.
     *
     * @param name     the display name
     * @param email    the email address
     * @param password the plaintext password
     * @param role     the requested role name (BUYER or SELLER)
     * @throws ValidationException if any field is invalid
     */
    public static void validateRegister(String name, String email, String password, String role)
            throws ValidationException {
        if (isBlank(name)) {
            throw new ValidationException("name", "Name is required.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new ValidationException("name", "Name must be at most " + NAME_MAX_LENGTH + " characters.");
        }
        if (isBlank(email)) {
            throw new ValidationException("email", "Email is required.");
        }
        if (email.length() > EMAIL_MAX_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("email", "A valid email address is required.");
        }
        if (isBlank(password)) {
            throw new ValidationException("password", "Password is required.");
        }
        if (password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            throw new ValidationException("password",
                    "Password must be between " + PASSWORD_MIN_LENGTH + " and " + PASSWORD_MAX_LENGTH + " characters.");
        }
        if (!containsLetter(password) || !containsDigit(password)) {
            throw new ValidationException("password", "Password must contain at least one letter and one digit.");
        }
        if (isBlank(role) || !isValidRole(role)) {
            throw new ValidationException("role", "Role must be BUYER or SELLER.");
        }
    }

    /**
     * Validates a login form.
     *
     * @param email    the email address
     * @param password the plaintext password
     * @throws ValidationException if any field is blank
     */
    public static void validateLogin(String email, String password) throws ValidationException {
        if (isBlank(email)) {
            throw new ValidationException("email", "Email is required.");
        }
        if (isBlank(password)) {
            throw new ValidationException("password", "Password is required.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isValidRole(String role) {
        return Role.BUYER.name().equals(role) || Role.SELLER.name().equals(role);
    }

    private static boolean containsLetter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLetter(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
