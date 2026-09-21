package com.sharvesh.sharvesh_mart.dao;

import java.util.List;
import java.util.Optional;

import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.model.CartItem;

/**
 * Data access contract for the {@code cart_items} table. All SQL lives in the
 * JDBC implementations and every statement is a PreparedStatement.
 */
public interface CartDao {

    /**
     * Returns all cart lines for a user.
     *
     * @param userId the user id
     * @return the cart lines
     * @throws DaoException if the query fails
     */
    List<CartItem> findByUser(int userId) throws DaoException;

    /**
     * Finds a cart line by primary key.
     *
     * @param id the cart line id
     * @return the cart line, or {@link Optional#empty()} if not found
     * @throws DaoException if the query fails
     */
    Optional<CartItem> findById(int id) throws DaoException;

    /**
     * Finds the cart line for a user and product (there is at most one per
     * user-product pair).
     *
     * @param userId    the user id
     * @param productId the product id
     * @return the cart line, or {@link Optional#empty()} if absent
     * @throws DaoException if the query fails
     */
    Optional<CartItem> findItem(int userId, int productId) throws DaoException;

    /**
     * Inserts a new cart line and assigns the generated id.
     *
     * @param item the cart line to persist (without an id)
     * @return the persisted line including the generated id
     * @throws DaoException if the insert fails
     */
    CartItem add(CartItem item) throws DaoException;

    /**
     * Updates the quantity of a cart line.
     *
     * @param id       the cart line id
     * @param quantity the new quantity
     * @throws DaoException if the update fails
     */
    void updateQuantity(int id, int quantity) throws DaoException;

    /**
     * Removes a cart line.
     *
     * @param id the cart line id
     * @throws DaoException if the delete fails
     */
    void remove(int id) throws DaoException;

    /**
     * Removes every cart line of a user.
     *
     * @param userId the user id
     * @throws DaoException if the delete fails
     */
    void clear(int userId) throws DaoException;

    /**
     * Counts the distinct lines in a user's cart.
     *
     * @param userId the user id
     * @return the number of lines
     * @throws DaoException if the query fails
     */
    int count(int userId) throws DaoException;
}
