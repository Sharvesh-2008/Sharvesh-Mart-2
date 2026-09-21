package com.sharvesh.sharvesh_mart.dto;

/**
 * Request shape carrying the login form fields.
 */
public class LoginRequest {

    private final String email;
    private final String password;

    /**
     * Creates a login request.
     *
     * @param email    the email address
     * @param password the plaintext password
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
