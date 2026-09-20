package com.saranya.saranyamart.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for bcrypt-based password hashing.
 */
class PasswordUtilTest {

    @Test
    void hashProducesBcryptHashAndVerifies() {
        String hash = PasswordUtil.hash("Password123");
        Assertions.assertTrue(hash.startsWith("$2a$"));
        Assertions.assertTrue(PasswordUtil.verify("Password123", hash));
    }

    @Test
    void verifyRejectsWrongPassword() {
        String hash = PasswordUtil.hash("Password123");
        Assertions.assertFalse(PasswordUtil.verify("WrongPassword", hash));
    }

    @Test
    void verifyRejectsNullOrEmptyInput() {
        Assertions.assertFalse(PasswordUtil.verify(null, "$2a$12$abcdefghijklmnopqrstuvwxyzabcdefghijklmn"));
        Assertions.assertFalse(PasswordUtil.verify("password", null));
        Assertions.assertFalse(PasswordUtil.verify("", ""));
    }

    @Test
    void verifyRejectsMalformedHash() {
        Assertions.assertFalse(PasswordUtil.verify("password", "not-a-bcrypt-hash"));
    }

    @Test
    void hashesAreUniqueAcrossCalls() {
        String first = PasswordUtil.hash("Password123");
        String second = PasswordUtil.hash("Password123");
        Assertions.assertNotEquals(first, second);
        Assertions.assertTrue(PasswordUtil.verify("Password123", first));
        Assertions.assertTrue(PasswordUtil.verify("Password123", second));
    }
}
