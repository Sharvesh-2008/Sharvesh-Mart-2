package com.sharvesh.sharvesh_mart.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.sharvesh.sharvesh_mart.dto.UserResponse;

/**
 * Session helpers for reading the signed-in user. The session is populated on
 * login by {@code AuthController} and enforced by {@code AuthFilter}, so every
 * protected route can rely on a user being present.
 */
public final class SessionUtil {

    /** Session attribute holding the signed-in {@link UserResponse}. */
    public static final String USER_KEY = "user";

    private SessionUtil() {
        // Utility class, not instantiable.
    }

    /**
     * Returns the signed-in user, or {@code null} if there is no session.
     *
     * @param request the HTTP request
     * @return the user, or {@code null}
     */
    public static UserResponse getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (UserResponse) session.getAttribute(USER_KEY);
    }
}
