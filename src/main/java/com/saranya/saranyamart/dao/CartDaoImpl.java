package com.saranya.saranyamart.dao;

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
import com.saranya.saranyamart.model.CartItem;

/**
 * JDBC implementation of {@link CartDao}. Every query is a PreparedStatement
 * and every resource is closed with try-with-resources.
 */
public class CartDaoImpl implements CartDao {

    private static final String COLUMNS = "id, user_id, product_id, quantity";

    private final DataSource dataSource;

    /**
     * Creates a DAO backed by the given connection pool.
     *
     * @param dataSource the pooled data source
     */
    public CartDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<CartItem> findByUser(int userId) throws DaoException {
        String sql = "SELECT " + COLUMNS + " FROM cart_items WHERE user_id = ? ORDER BY id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CartItem> items = new ArrayList<>();
                while (resultSet.next()) {
                    items.add(mapRow(resultSet));
                }
                return items;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to load cart for user " + userId, e);
        }
    }

    @Override
    public Optional<CartItem> findById(int id) throws DaoException {
        String sql = "SELECT " + COLUMNS + " FROM cart_items WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find cart item by id " + id, e);
        }
    }

    @Override
    public Optional<CartItem> findItem(int userId, int productId) throws DaoException {
        String sql = "SELECT " + COLUMNS + " FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find cart item for user " + userId + " and product " + productId, e);
        }
    }

    @Override
    public CartItem add(CartItem item) throws DaoException {
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, item.getUserId());
            statement.setInt(2, item.getProductId());
            statement.setInt(3, item.getQuantity());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1)).orElseThrow(
                            () -> new DaoException("Added cart item could not be re-read."));
                }
                throw new DaoException("No generated key returned for new cart item.");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to add product " + item.getProductId()
                    + " to cart for user " + item.getUserId(), e);
        }
    }

    @Override
    public void updateQuantity(int id, int quantity) throws DaoException {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update quantity for cart item " + id, e);
        }
    }

    @Override
    public void remove(int id) throws DaoException {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to remove cart item " + id, e);
        }
    }

    @Override
    public void clear(int userId) throws DaoException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to clear cart for user " + userId, e);
        }
    }

    @Override
    public int count(int userId) throws DaoException {
        String sql = "SELECT COUNT(*) FROM cart_items WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to count cart items for user " + userId, e);
        }
    }

    private CartItem mapRow(ResultSet resultSet) throws SQLException {
        CartItem item = new CartItem();
        item.setId(resultSet.getInt("id"));
        item.setUserId(resultSet.getInt("user_id"));
        item.setProductId(resultSet.getInt("product_id"));
        item.setQuantity(resultSet.getInt("quantity"));
        return item;
    }
}
