package com.sharvesh.sharvesh_mart.listener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Owns the connection pool lifecycle and initializes the database schema and
 * seed data on startup (spec Section 2/3/4). This is the only place a
 * {@code DataSource} is created; no {@code DriverManager} calls exist elsewhere.
 */
public class AppContextListener implements ServletContextListener {

    /** ServletContext attribute holding the pooled data source. */
    public static final String DATA_SOURCE_KEY = "SharveshMart.datasource";

    private static final Logger LOGGER = LoggerFactory.getLogger(AppContextListener.class);

    private static final String SCHEMA_RESOURCE = "migrations/V1__init_schema.sql";
    private static final String SEED_RESOURCE = "seed.sql";

    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Config config = Config.load();
        ensureDataDirectory(config.jdbcUrl);
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("H2 JDBC driver not found on classpath", e);
        }
        dataSource = new HikariDataSource(buildPoolConfig(config));
        ServletContext context = event.getServletContext();
        context.setAttribute(DATA_SOURCE_KEY, dataSource);
        initializeDatabase(dataSource, config.resetOnStartup);
        LOGGER.info("SharveshMart application context initialized (pool size {})", config.poolSize);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (dataSource != null) {
            dataSource.close();
        }
        LOGGER.info("SharveshMart application context destroyed.");
    }

    private HikariConfig buildPoolConfig(Config config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.jdbcUrl);
        hikariConfig.setUsername(config.username);
        hikariConfig.setPassword(config.password);
        hikariConfig.setMaximumPoolSize(config.poolSize);
        hikariConfig.setPoolName("SharveshMart-pool");
        return hikariConfig;
    }

    private void ensureDataDirectory(String jdbcUrl) {
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2:file:")) {
            File dataDirectory = new File("data");
            if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
                LOGGER.warn("Could not create the local data directory: {}", dataDirectory.getAbsolutePath());
            }
        }
    }

    private void initializeDatabase(HikariDataSource pool, boolean resetOnStartup) {
        try (Connection connection = pool.getConnection()) {
            if (resetOnStartup) {
                RunScript.execute(connection, new StringReader("DROP ALL OBJECTS;"));
                LOGGER.warn("DB_RESET_ON_STARTUP=true: dropped all objects.");
            }
            runScript(connection, SCHEMA_RESOURCE);
            if (isEmpty(connection)) {
                runScript(connection, SEED_RESOURCE);
                LOGGER.info("Database initialized with schema and seed data.");
            } else {
                LOGGER.info("Database already populated; seed data skipped.");
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed to initialize the SharveshMart database.", e);
        }
    }

    private void runScript(Connection connection, String classpathResource)
            throws SQLException, IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(classpathResource);
        if (inputStream == null) {
            throw new IOException("Classpath resource not found: " + classpathResource);
        }
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            RunScript.execute(connection, reader);
        }
    }

    private boolean isEmpty(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getLong(1) == 0L;
        }
    }

    /**
     * Runtime configuration resolved from environment variables first and then
     * from {@code config.properties} (gitignored) with sensible local defaults.
     */
    private static final class Config {

        private final String jdbcUrl;
        private final String username;
        private final String password;
        private final int poolSize;
        private final boolean resetOnStartup;

        private Config(String jdbcUrl, String username, String password, int poolSize, boolean resetOnStartup) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
            this.poolSize = poolSize;
            this.resetOnStartup = resetOnStartup;
        }

        static Config load() {
            Properties properties = new Properties();
            try (InputStream inputStream =
                         AppContextListener.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (inputStream != null) {
                    properties.load(inputStream);
                }
            } catch (IOException e) {
                LOGGER.warn("Could not read config.properties; using defaults.", e);
            }
            String jdbcUrl = firstNonNull(
                    System.getenv("DB_URL"), properties.getProperty("jdbc.url"), "jdbc:h2:file:./data/SharveshMart");
            String username = firstNonNull(
                    System.getenv("DB_USER"), properties.getProperty("jdbc.username"), "sa");
            String password = firstNonNull(
                    System.getenv("DB_PASSWORD"), properties.getProperty("jdbc.password"), "");
            int poolSize = Integer.parseInt(firstNonNull(
                    System.getenv("DB_POOL_SIZE"), properties.getProperty("db.pool.size"), "10"));
            boolean resetOnStartup = Boolean.parseBoolean(firstNonNull(
                    System.getenv("DB_RESET_ON_STARTUP"), properties.getProperty("db.reset.on.startup"), "false"));
            return new Config(jdbcUrl, username, password, poolSize, resetOnStartup);
        }

        private static String firstNonNull(String envValue, String propertyValue, String fallback) {
            if (envValue != null && !envValue.isEmpty()) {
                return envValue;
            }
            if (propertyValue != null && !propertyValue.isEmpty()) {
                return propertyValue;
            }
            return fallback;
        }
    }
}
