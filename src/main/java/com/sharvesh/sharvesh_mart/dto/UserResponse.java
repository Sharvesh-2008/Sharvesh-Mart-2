package com.sharvesh.sharvesh_mart.dto;

import com.sharvesh.sharvesh_mart.model.Role;
import com.sharvesh.sharvesh_mart.model.User;

/**
 * Response shape for a user. Never carries the password hash
 * (spec Section 13 rule 4).
 */
public class UserResponse {

    private final Integer id;
    private final String name;
    private final String email;
    private final Role role;
    private final String createdAt;

    private UserResponse(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.role = builder.role;
        this.createdAt = builder.createdAt;
    }

    /**
     * Builds a response DTO from an entity.
     *
     * @param user the user entity
     * @return the response DTO
     */
    public static UserResponse from(User user) {
        return new Builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt() == null ? null : user.getCreatedAt().toString())
                .build();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Builder for {@link UserResponse} (spec Section 12 design patterns).
     */
    public static final class Builder {

        private Integer id;
        private String name;
        private String email;
        private Role role;
        private String createdAt;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Produces an immutable {@link UserResponse}.
         *
         * @return the built response
         */
        public UserResponse build() {
            return new UserResponse(this);
        }
    }
}
