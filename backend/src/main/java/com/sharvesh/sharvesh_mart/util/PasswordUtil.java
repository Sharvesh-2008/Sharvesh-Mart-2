package com.sharvesh.sharvesh_mart.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing helpers built on jBCrypt. Plaintext passwords are never
 * stored or logged (spec Section 9 security checklist).
 */
public final class PasswordUtil {

    /** Work factor used by the bcrypt hash function. */
    private static final int BCRYPT_COST = 12;

    private PasswordUtil() {
        // Utility class, not instantiable.
    }

    /**
     * Hashes a plaintext password with bcrypt (random salt, cost 12).
     *
     * @param plain the plaintext password
     * @return the bcrypt hash
     */
    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Verifies a plaintext password against a stored bcrypt hash.
     *
     * @param plain the plaintext password supplied by the user
     * @param hash  the stored bcrypt hash
     * @return {@code true} if the password matches, {@code false} otherwise
     */
    public static boolean verify(String plain, String hash) {
        if (plain == null || hash == null || plain.isEmpty() || hash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
