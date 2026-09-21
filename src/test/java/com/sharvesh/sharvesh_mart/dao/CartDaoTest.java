package com.sharvesh.sharvesh_mart.dao;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Optional;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.model.CartItem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DAO tests for the cart against an embedded H2 instance initialized from the
 * checked-in schema and seed scripts.
 */
class CartDaoTest {

    private static final String TEST_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final int BUYER_ID = 3;

    private static HikariDataSource dataSource;
    private static CartDao cartDao;

    @BeforeAll
    static void setUpPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(TEST_URL);
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new HikariDataSource(config);
        cartDao = new CartDaoImpl(dataSource);
    }

    @AfterAll
    static void tearDownPool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @BeforeEach
    void resetDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            RunScript.execute(connection, new StringReader("DROP ALL OBJECTS"));
            RunScript.execute(connection, script("migrations/V1__init_schema.sql"));
            RunScript.execute(connection, script("seed.sql"));
        }
    }

    @Test
    void addPersistsLineAndReReadsIt() throws Exception {
        CartItem added = cartDao.add(newLine());

        Assertions.assertNotNull(added.getId());
        Optional<CartItem> reloaded = cartDao.findItem(BUYER_ID, added.getProductId());
        Assertions.assertTrue(reloaded.isPresent());
        Assertions.assertEquals(2, reloaded.get().getQuantity());
    }

    @Test
    void addThenUpdateQuantity() throws Exception {
        CartItem added = cartDao.add(newLine());

        cartDao.updateQuantity(added.getId(), 5);

        Optional<CartItem> reloaded = cartDao.findById(added.getId());
        Assertions.assertTrue(reloaded.isPresent());
        Assertions.assertEquals(5, reloaded.get().getQuantity());
    }

    @Test
    void removeDeletesLine() throws Exception {
        CartItem added = cartDao.add(newLine());

        cartDao.remove(added.getId());

        Assertions.assertTrue(cartDao.findById(added.getId()).isEmpty());
        Assertions.assertTrue(cartDao.findByUser(BUYER_ID).isEmpty());
    }

    @Test
    void clearRemovesAllLines() throws Exception {
        cartDao.add(newLine());
        CartItem other = newLine();
        other.setProductId(2);
        cartDao.add(other);

        cartDao.clear(BUYER_ID);

        Assertions.assertTrue(cartDao.findByUser(BUYER_ID).isEmpty());
        Assertions.assertEquals(0, cartDao.count(BUYER_ID));
    }

    @Test
    void countReflectsLinesPerUser() throws Exception {
        cartDao.add(newLine());
        CartItem other = newLine();
        other.setProductId(2);
        cartDao.add(other);

        Assertions.assertEquals(2, cartDao.count(BUYER_ID));
    }

    @Test
    void duplicateUserProductLineIsRejected() throws Exception {
        cartDao.add(newLine());

        Assertions.assertThrows(DaoException.class, () -> cartDao.add(newLine()));
    }

    private CartItem newLine() {
        CartItem item = new CartItem();
        item.setUserId(BUYER_ID);
        item.setProductId(1);
        item.setQuantity(2);
        return item;
    }

    private static Reader script(String resource) throws Exception {
        InputStream stream = CartDaoTest.class.getClassLoader().getResourceAsStream(resource);
        Assertions.assertNotNull(stream, "Missing classpath resource: " + resource);
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }
}
