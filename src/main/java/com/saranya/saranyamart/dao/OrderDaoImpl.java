package com.saranya.saranyamart.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.saranya.saranyamart.exception.DaoException;
import com.saranya.saranyamart.exception.InsufficientStockException;
import com.saranya.saranyamart.model.Order;
import com.saranya.saranyamart.model.OrderItem;
import com.saranya.saranyamart.model.OrderStatus;

/**
 * JDBC implementation of {@link OrderDao}. Every query is a PreparedStatement
 * and every resource is closed with try-with-resources. {@link #placeOrder}
 * runs on a single connection with manual commit so stock updates and order
 * inserts are atomic (design rule: service layer has no JDBC).
 */
public class OrderDaoImpl implements OrderDao {

    private static final String ORDER_COLUMNS = "id, buyer_id, status, total_amount, created_at";

    private final DataSource dataSource;

    /**
     * Creates a DAO backed by the given connection pool.
     *
     * @param dataSource the pooled data source
     */
    public OrderDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Order placeOrder(int buyerId, List<OrderItem> items) throws DaoException, InsufficientStockException {
        if (items == null || items.isEmpty()) {
            throw new DaoException("Cannot place an order without items.");
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            int orderId;
            try {
                orderId = insertOrderInTransaction(connection, buyerId, items);
            } catch (InsufficientStockException e) {
                rollback(connection);
                throw e;
            } catch (SQLException e) {
                rollback(connection);
                throw new DaoException("Failed to place order for buyer " + buyerId, e);
            }
            connection.commit();
            return findById(orderId).orElseThrow(
                    () -> new DaoException("Placed order could not be re-read: " + orderId));
        } catch (SQLException e) {
            throw new DaoException("Failed to place order for buyer " + buyerId, e);
        }
    }

    @Override
    public Optional<Order> findById(int id) throws DaoException {
        String sql = "SELECT " + ORDER_COLUMNS + " FROM orders WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapOrder(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find order by id " + id, e);
        }
    }

    @Override
    public List<Order> findByBuyer(int buyerId) throws DaoException {
        String sql = "SELECT " + ORDER_COLUMNS + " FROM orders WHERE buyer_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, buyerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }
                return orders;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to load orders for buyer " + buyerId, e);
        }
    }

    @Override
    public List<OrderItem> findItemsByOrder(int orderId) throws DaoException {
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, p.name AS product_name "
                + "FROM order_items oi JOIN products p ON p.id = oi.product_id WHERE oi.order_id = ? ORDER BY oi.id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (resultSet.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(resultSet.getInt("id"));
                    item.setOrderId(resultSet.getInt("order_id"));
                    item.setProductId(resultSet.getInt("product_id"));
                    item.setQuantity(resultSet.getInt("quantity"));
                    item.setUnitPrice(resultSet.getBigDecimal("unit_price"));
                    item.setProductName(resultSet.getString("product_name"));
                    items.add(item);
                }
                return items;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to load items for order " + orderId, e);
        }
    }

    @Override
    public List<Order> findBySeller(int sellerId) throws DaoException {
        String sql = "SELECT DISTINCT o.id, o.buyer_id, o.status, o.total_amount, o.created_at "
                + "FROM orders o "
                + "JOIN order_items oi ON oi.order_id = o.id "
                + "JOIN products p ON p.id = oi.product_id "
                + "WHERE p.seller_id = ? "
                + "ORDER BY o.created_at DESC, o.id DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sellerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }
                return orders;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to load orders for seller " + sellerId, e);
        }
    }

    private int insertOrderInTransaction(Connection connection, int buyerId, List<OrderItem> items)
            throws SQLException, InsufficientStockException {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            decrementStock(connection, item.getProductId(), item.getQuantity());
            item.setUnitPrice(findPrice(connection, item.getProductId()));
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        String sql = "INSERT INTO orders (buyer_id, status, total_amount) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, buyerId);
            statement.setString(2, OrderStatus.CONFIRMED.name());
            statement.setBigDecimal(3, total);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for new order.");
                }
                int orderId = keys.getInt(1);
                insertOrderItems(connection, orderId, items);
                return orderId;
            }
        }
    }

    private void insertOrderItems(Connection connection, int orderId, List<OrderItem> items) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (OrderItem item : items) {
                statement.setInt(1, orderId);
                statement.setInt(2, item.getProductId());
                statement.setInt(3, item.getQuantity());
                statement.setBigDecimal(4, item.getUnitPrice());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void decrementStock(Connection connection, int productId, int quantity)
            throws SQLException, InsufficientStockException {
        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            if (statement.executeUpdate() == 0) {
                throw new InsufficientStockException(productId, quantity);
            }
        }
    }

    private BigDecimal findPrice(Connection connection, int productId) throws SQLException {
        String sql = "SELECT price FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Product not found for price lookup: " + productId);
                }
                return resultSet.getBigDecimal(1);
            }
        }
    }

    private Order mapOrder(ResultSet resultSet) throws SQLException {
        Order order = new Order();
        order.setId(resultSet.getInt("id"));
        order.setBuyerId(resultSet.getInt("buyer_id"));
        order.setStatus(OrderStatus.valueOf(resultSet.getString("status")));
        order.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        order.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return order;
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to roll back order transaction.", e);
        }
    }
}
