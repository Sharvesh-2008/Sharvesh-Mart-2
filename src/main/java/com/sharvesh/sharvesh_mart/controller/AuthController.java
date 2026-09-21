package com.sharvesh.sharvesh_mart.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sharvesh.sharvesh_mart.dto.LoginRequest;
import com.sharvesh.sharvesh_mart.dto.RegisterRequest;
import com.sharvesh.sharvesh_mart.dto.UserResponse;
import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.exception.DuplicateEmailException;
import com.sharvesh.sharvesh_mart.exception.ValidationException;
import com.sharvesh.sharvesh_mart.model.User;
import com.sharvesh.sharvesh_mart.service.UserService;

/**
 * Thin controller for the authentication flows: register, login, logout and
 * the protected landing page (requirement F1). Session id is regenerated on
 * login by invalidating the previous session (spec Section 4 rule 3).
 */
public class AuthController {

    private static final String SESSION_USER_KEY = "user";
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private final UserService userService;

    /**
     * Creates the controller with the user service.
     *
     * @param userService the user service
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Root path: sends signed-in users home and everyone else to the login form.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a redirect target
     */
    public String index(HttpServletRequest request, HttpServletResponse response) {
        return isLoggedIn(request) ? "redirect:/home" : "redirect:/login";
    }

    /**
     * Renders the login form.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the login view
     */
    public String showLogin(HttpServletRequest request, HttpServletResponse response) {
        return "login.jsp";
    }

    /**
     * Authenticates a user and opens a fresh session on success.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a redirect, or the login view with an error
     * @throws DaoException if persistence fails
     */
    public String login(HttpServletRequest request, HttpServletResponse response) throws DaoException {
        try {
            LoginRequest form = new LoginRequest(request.getParameter("email"), request.getParameter("password"));
            Optional<User> user = userService.authenticate(form.getEmail(), form.getPassword());
            if (user.isEmpty()) {
                return renderLogin(request, "Invalid email or password.", form.getEmail());
            }
            openSession(request, user.get());
            return redirectTo(request);
        } catch (ValidationException e) {
            return renderLogin(request, e.getMessage(), request.getParameter("email"));
        }
    }

    /**
     * Renders the registration form.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the register view
     */
    public String showRegister(HttpServletRequest request, HttpServletResponse response) {
        return "register.jsp";
    }

    /**
     * Registers a new Buyer or Seller account, then sends the user to log in.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a redirect, or the register view with field errors
     * @throws DaoException if persistence fails
     */
    public String register(HttpServletRequest request, HttpServletResponse response) throws DaoException {
        try {
            RegisterRequest form = new RegisterRequest(
                    request.getParameter("name"),
                    request.getParameter("email"),
                    request.getParameter("password"),
                    request.getParameter("role"));
            userService.register(form);
            return "redirect:/login?registered=1";
        } catch (ValidationException e) {
            return renderRegister(request, e.getMessage(), e.getField());
        } catch (DuplicateEmailException e) {
            return renderRegister(request, e.getMessage(), "email");
        }
    }

    /**
     * Invalidates the current session and returns to the login form.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a redirect to the login form
     */
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?loggedout=1";
    }

    /**
     * Renders the protected landing page for signed-in users.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the home view
     */
    public String home(HttpServletRequest request, HttpServletResponse response) {
        return "home.jsp";
    }

    private void openSession(HttpServletRequest request, User user) {
        HttpSession previous = request.getSession(false);
        if (previous != null) {
            previous.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute(SESSION_USER_KEY, UserResponse.from(user));
    }

    private String redirectTo(HttpServletRequest request) {
        String target = request.getParameter("redirect");
        if (target != null && target.startsWith("/") && !target.startsWith("//")) {
            return "redirect:" + target;
        }
        return "redirect:/home";
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(SESSION_USER_KEY) != null;
    }

    private String renderLogin(HttpServletRequest request, String error, String email) {
        request.setAttribute("error", error);
        request.setAttribute("email", email);
        return "login.jsp";
    }

    private String renderRegister(HttpServletRequest request, String error, String field) {
        request.setAttribute("error", error);
        request.setAttribute("field", field);
        request.setAttribute("name", request.getParameter("name"));
        request.setAttribute("email", request.getParameter("email"));
        request.setAttribute("role", request.getParameter("role"));
        return "register.jsp";
    }
}
