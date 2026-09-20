package com.saranya.saranyamart.dao;

import java.util.List;
import java.util.Optional;

import com.saranya.saranyamart.exception.DaoException;
import com.saranya.saranyamart.exception.InsufficientStockException;
import com.saranya.saranyamart.model.Order;
import com.saranya.saranyamart.model.OrderItem;

/**
 * Data access contract for the {@code orders} and {@code order_items} tables.
 * All SQL lives in the JDBC implementation and every statement is a
 * PreparedStatement. Order placement is atomic: stock is decremented and the
 * order rows are inserted on a single transaction.
 */
public interface OrderDao {

    /**
     * Places an order atomically. Validates stock, decrements product
     * quantities, inserts the order header and its lines, and returns the
     * persisted order. Any failure rolls the whole transaction back.
     *
     * @param buyerId the buyer's user id
     * @param items   the requested lines (product id + quantity)
     * @return the persisted order (status CONFIRMED)
     * @throws DaoException              if persistence fails
     * @throws InsufficientStockException if any line cannot be fulfilled
     */
    Order placeOrder(int buyerId, List<OrderItem> items) throws DaoException, InsufficientStockException;

    /**
     * Finds an order by primary key.
     *
     * @param id the order id
     * @return the order, or {@link Optional#empty()} if not found
     * @throws DaoException if the query fails
     */
    Optional<Order> findById(int id) throws DaoException;

    /**
     * Returns all orders placed by a buyer, newest first.
     *
     * @param buyerId the buyer's user id
     * @return the orders
     * @throws DaoException if the query fails
     */
    List<Order> findByBuyer(int buyerId) throws DaoException;

    /**
     * Returns the lines of an order, including each product's name.
     *
     * @param orderId the order id
     * @return the order lines
     * @throws DaoException if the query fails
     */
    List<OrderItem> findItemsByOrder(int orderId) throws DaoException;

    /**
     * Returns the distinct orders that contain at least one product owned by the
     * seller, newest first (used for the seller's order history).
     *
     * @param sellerId the seller's user id
     * @return the orders
     * @throws DaoException if the query fails
     */
    List<Order> findBySeller(int sellerId) throws DaoException;
}
