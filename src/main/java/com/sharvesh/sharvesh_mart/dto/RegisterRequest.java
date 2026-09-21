package com.sharvesh.sharvesh_mart.dto;

/**
 * Request shape carrying the registration form fields (requirement F1).
 */
public class RegisterRequest {

    private final String name;
    private final String email;
    private final String password;
    private final String role;

    /**
     * Creates a registration request.
     *
     * @param name     the display name
     * @param email    the email address
     * @param password the plaintext password
     * @param role     the requested role name (BUYER or SELLER)
     */
    public RegisterRequest(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}
