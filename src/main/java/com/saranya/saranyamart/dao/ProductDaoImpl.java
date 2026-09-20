package com.saranya.saranyamart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.saranya.saranyamart.exception.DaoException;
import com.saranya.saranyamart.model.Product;

/**
 * JDBC implementation of {@link ProductDao}. Every query is a PreparedStatement
 * and every resource is closed with try-with-resources.
 */
public class ProductDaoImpl implements ProductDao {

    private final DataSource dataSource;

    /**
     * Creates a DAO backed by the given connection pool.
     *
     * @param dataSource the pooled data source
     */
    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Product> findById(int id) throws DaoException {
        String sql = "SELECT " + ProductDao.COLUMNS + " FROM products WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find product by id " + id, e);
        }
    }

    @Override
    public List<Product> search(String keyword, String category) throws DaoException {
        StringBuilder sql = new StringBuilder("SELECT " + ProductDao.COLUMNS + " FROM products WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND UPPER(name) LIKE UPPER(?)");
            params.add("%" + keyword.trim() + "%");
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND UPPER(category) = UPPER(?)");
            params.add(category.trim());
        }
        sql.append(" ORDER BY created_at DESC, id DESC");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i + 1, (String) params.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (resultSet.next()) {
                    products.add(mapRow(resultSet));
                }
                return products;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to search products", e);
        }
    }

    @Override
    public List<String> findCategories() throws DaoException {
        String sql = "SELECT DISTINCT category FROM products "
                + "WHERE category IS NOT NULL AND category <> '' ORDER BY category";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<String> categories = new ArrayList<>();
            while (resultSet.next()) {
                categories.add(resultSet.getString(1));
            }
            return categories;
        } catch (SQLException e) {
            throw new DaoException("Failed to list product categories", e);
        }
    }

    private Product mapRow(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getInt("id"));
        product.setSellerId(resultSet.getInt("seller_id"));
        product.setName(resultSet.getString("name"));
        product.setDescription(resultSet.getString("description"));
        product.setPrice(resultSet.getBigDecimal("price"));
        product.setStockQty(resultSet.getInt("stock_qty"));
        product.setCategory(resultSet.getString("category"));
        product.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return product;
    }

    @Override
    public List<Product> findBySeller(int sellerId) throws DaoException {
        String sql = "SELECT " + ProductDao.COLUMNS
                + " FROM products WHERE seller_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sellerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (resultSet.next()) {
                    products.add(mapRow(resultSet));
                }
                return products;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find products for seller " + sellerId, e);
        }
    }

    @Override
    public Product create(Product product) throws DaoException {
        String sql = "INSERT INTO products (seller_id, name, description, "
                + "price, stock_qty, category) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,
                     java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, product.getSellerId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getStockQty());
            statement.setString(6, product.getCategory());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1)).orElseThrow(
                            () -> new DaoException("Created product could not be re-read."));
                }
                throw new DaoException("No generated key returned for new product.");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to create product", e);
        }
    }

    @Override
    public void update(Product product) throws DaoException {
        String sql = "UPDATE products SET name = ?, description = ?, "
                + "price = ?, stock_qty = ?, category = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setInt(4, product.getStockQty());
            statement.setString(5, product.getCategory());
            statement.setInt(6, product.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update product " + product.getId(), e);
        }
    }

    @Override
    public void delete(int id) throws DaoException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete product " + id, e);
        }
    }

    @Override
    public boolean isReferenced(int productId) throws DaoException {
        String sql = "SELECT COUNT(*) FROM ("
                + "SELECT product_id FROM cart_items WHERE product_id = ?"
                + " UNION ALL "
                + "SELECT product_id FROM order_items WHERE product_id = ?"
                + ") ref";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to check references for product " + productId, e);
        }
    }
}
