package com.saranya.saranyamart.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.saranya.saranyamart.exception.ValidationException;

/**
 * Unit tests for field-level input validation.
 */
class ValidationUtilTest {

    @Test
    void validRegisterInputPasses() {
        Assertions.assertDoesNotThrow(
                () -> ValidationUtil.validateRegister("Alice", "alice@example.com", "Password123", "BUYER"));
    }

    @Test
    void blankNameFailsWithNameField() {
        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRegister("  ", "alice@example.com", "Password123", "BUYER"));
        Assertions.assertEquals("name", exception.getField());
    }

    @Test
    void invalidEmailFailsWithEmailField() {
        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRegister("Alice", "not-an-email", "Password123", "BUYER"));
        Assertions.assertEquals("email", exception.getField());
    }

    @Test
    void shortPasswordFailsWithPasswordField() {
        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRegister("Alice", "alice@example.com", "short", "BUYER"));
        Assertions.assertEquals("password", exception.getField());
    }

    @Test
    void passwordWithoutDigitFails() {
        Assertions.assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRegister("Alice", "alice@example.com", "abcdefgh", "BUYER"));
    }

    @Test
    void passwordWithoutLetterFails() {
        Assertions.assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRegister("Alice", "alice@example.com", "12345678", "BUYER"));
    }

    @Test
    void invalidRoleFailsWithRoleField() {
        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRegister("Alice", "alice@example.com", "Password123", "ADMIN"));
        Assertions.assertEquals("role", exception.getField());
    }

    @Test
    void validLoginInputPasses() {
        Assertions.assertDoesNotThrow(() -> ValidationUtil.validateLogin("alice@example.com", "Password123"));
    }

    @Test
    void blankLoginEmailFails() {
        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> ValidationUtil.validateLogin("", "Password123"));
        Assertions.assertEquals("email", exception.getField());
    }

    @Test
    void blankLoginPasswordFails() {
        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> ValidationUtil.validateLogin("alice@example.com", ""));
        Assertions.assertEquals("password", exception.getField());
    }
}
