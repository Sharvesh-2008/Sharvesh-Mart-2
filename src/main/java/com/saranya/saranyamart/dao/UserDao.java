package com.saranya.saranyamart.dao;

import java.util.Optional;

import com.saranya.saranyamart.exception.DaoException;
import com.saranya.saranyamart.model.User;

/**
 * Data access contract for the {@code users} table. All SQL lives in the JDBC
 * implementations of this interface and every statement is a PreparedStatement.
 */
public interface UserDao {

    /**
     * Finds a user by primary key.
     *
     * @param id the user id
     * @return the user, or {@link Optional#empty()} if not found
     * @throws DaoException if the query fails
     */
    Optional<User> findById(int id) throws DaoException;

    /**
     * Finds a user by (normalized) email address.
     *
     * @param email the email address
     * @return the user, or {@link Optional#empty()} if not found
     * @throws DaoException if the query fails
     */
    Optional<User> findByEmail(String email) throws DaoException;

    /**
     * Inserts a new user and assigns the generated id and created-at timestamp.
     *
     * @param user the user to persist (without an id)
     * @return the persisted user including the generated id
     * @throws DaoException if the insert fails
     */
    User create(User user) throws DaoException;

    /**
     * Returns whether a user with the given email address exists.
     *
     * @param email the email address
     * @return {@code true} if present, {@code false} otherwise
     * @throws DaoException if the query fails
     */
    boolean emailExists(String email) throws DaoException;

    /**
     * Returns the total number of users.
     *
     * @return the user count
     * @throws DaoException if the query fails
     */
    long count() throws DaoException;
}
