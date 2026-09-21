package com.sharvesh.sharvesh_mart.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sharvesh.sharvesh_mart.dto.UserResponse;
import com.sharvesh.sharvesh_mart.model.Role;

/**
 * Enforces session-based authentication on protected routes (spec Section 9
 * security checklist). Public routes and error dispatches pass through.
 * Requests to {@code /admin/**} additionally require the ADMIN role.
 */
public class AuthFilter implements Filter {

    private static final String SESSION_USER_KEY = "user";
    private static final Set<String> PUBLIC_PREFIXES =
            Set.of("/", "/login", "/register", "/static/", "/api/v1/health");

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isPublic(httpRequest) || isErrorDispatch(httpRequest) || isAuthenticated(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        if (isAdminPath(httpRequest) && !isAdmin(httpRequest)) {
            redirectToLogin(httpRequest, httpResponse);
            return;
        }

        redirectToLogin(httpRequest, httpResponse);
    }

    @Override
    public void destroy() {
        // No cleanup required.
    }

    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isErrorDispatch(HttpServletRequest request) {
        return DispatcherType.ERROR.equals(request.getDispatcherType());
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(SESSION_USER_KEY) != null;
    }

    private boolean isAdminPath(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/admin");
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_KEY) == null) {
            return false;
        }
        return Role.ADMIN.equals(((UserResponse) session.getAttribute(SESSION_USER_KEY)).getRole());
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String target = request.getRequestURI().substring(request.getContextPath().length());
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            target = target + "?" + query;
        }
        String encoded = URLEncoder.encode(target, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login?redirect=" + encoded);
    }
}
