package com.sharvesh.sharvesh_mart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.model.Role;
import com.sharvesh.sharvesh_mart.model.User;

/**
 * JDBC implementation of {@link UserDao}. Every query is a PreparedStatement
 * and every resource is closed with try-with-resources (spec Section 4 rules).
 */
public class UserDaoImpl implements UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

    private static final String COLUMNS = "id, name, email, password_hash, role, created_at";

    private final DataSource dataSource;

    /**
     * Creates a DAO backed by the given connection pool.
     *
     * @param dataSource the pooled data source
     */
    public UserDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<User> findById(int id) throws DaoException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find user by id " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DaoException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find user by email " + email, e);
        }
    }

    @Override
    public User create(User user) throws DaoException {
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return findById(id).orElseThrow(() -> new DaoException("Created user could not be re-read."));
                }
                throw new DaoException("No generated key returned for new user.");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to create user with email " + user.getEmail(), e);
        }
    }

    @Override
    public boolean emailExists(String email) throws DaoException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to check email existence for " + email, e);
        }
    }

    @Override
    public long count() throws DaoException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DaoException("Failed to count users", e);
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(Role.valueOf(resultSet.getString("role")));
        user.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}
