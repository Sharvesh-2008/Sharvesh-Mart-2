package com.saranya.saranyamart.service;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.saranya.saranyamart.dao.UserDao;
import com.saranya.saranyamart.dto.RegisterRequest;
import com.saranya.saranyamart.dto.UserResponse;
import com.saranya.saranyamart.exception.DaoException;
import com.saranya.saranyamart.exception.DuplicateEmailException;
import com.saranya.saranyamart.exception.ValidationException;
import com.saranya.saranyamart.model.Role;
import com.saranya.saranyamart.model.User;
import com.saranya.saranyamart.util.PasswordUtil;
import com.saranya.saranyamart.util.ValidationUtil;

/**
 * Orchestrates the authentication business rules (requirement F1). Depends on
 * the {@link UserDao} interface only, never a concrete DAO class.
 */
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;

    /**
     * Creates a service backed by the given user DAO.
     *
     * @param userDao the user data access object
     */
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Registers a new Buyer or Seller account. Validates input first, rejects
     * duplicate email addresses, and stores only a bcrypt hash of the password.
     *
     * @param request the registration form
     * @return the created account, without the password hash
     * @throws ValidationException    if any field is invalid (before any DAO call)
     * @throws DuplicateEmailException if the email is already registered
     * @throws DaoException           if persistence fails
     */
    public UserResponse register(RegisterRequest request)
            throws ValidationException, DuplicateEmailException, DaoException {
        ValidationUtil.validateRegister(
                request.getName(), request.getEmail(), request.getPassword(), request.getRole());
        String normalizedEmail = normalizeEmail(request.getEmail());
        if (userDao.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateEmailException(normalizedEmail);
        }
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(PasswordUtil.hash(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));
        User created = userDao.create(user);
        LOGGER.info("Registered new {} account: {}", created.getRole(), created.getEmail());
        return UserResponse.from(created);
    }

    /**
     * Authenticates a user against the stored bcrypt hash. Does not reveal
     * whether the email or the password was wrong.
     *
     * @param email    the email address
     * @param password the plaintext password
     * @return the authenticated user, or {@link Optional#empty()} on failure
     * @throws ValidationException if the form fields are blank
     * @throws DaoException        if persistence fails
     */
    public Optional<User> authenticate(String email, String password)
            throws ValidationException, DaoException {
        ValidationUtil.validateLogin(email, password);
        Optional<User> user = userDao.findByEmail(normalizeEmail(email));
        if (user.isEmpty() || !PasswordUtil.verify(password, user.get().getPasswordHash())) {
            return Optional.empty();
        }
        return user;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
