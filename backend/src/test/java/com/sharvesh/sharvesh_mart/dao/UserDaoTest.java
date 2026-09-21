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

import com.sharvesh.sharvesh_mart.model.Role;
import com.sharvesh.sharvesh_mart.model.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DAO tests against an embedded H2 instance initialized from the checked-in
 * schema script on every test run (spec Section 9 testing requirements).
 */
class UserDaoTest {

    private static final String TEST_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    private static HikariDataSource dataSource;
    private static UserDao userDao;

    @BeforeAll
    static void setUpPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(TEST_URL);
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new HikariDataSource(config);
        userDao = new UserDaoImpl(dataSource);
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
    void createPersistsUserAndReReadsIt() throws Exception {
        User user = new User();
        user.setName("New Buyer");
        user.setEmail("newbuyer@example.com");
        user.setPasswordHash("$2a$12$dummyhashdummyhashdummyhashdummyhashdummy");
        user.setRole(Role.BUYER);

        User created = userDao.create(user);

        Assertions.assertNotNull(created.getId());
        Optional<User> reloaded = userDao.findById(created.getId());
        Assertions.assertTrue(reloaded.isPresent());
        Assertions.assertEquals("newbuyer@example.com", reloaded.get().getEmail());
        Assertions.assertEquals(Role.BUYER, reloaded.get().getRole());
        Assertions.assertNotNull(reloaded.get().getCreatedAt());
    }

    @Test
    void findByEmailFindsSeededAdmin() throws Exception {
        Optional<User> admin = userDao.findByEmail("admin@sharveshmart.com");

        Assertions.assertTrue(admin.isPresent());
        Assertions.assertEquals(Role.ADMIN, admin.get().getRole());
        Assertions.assertEquals("Admin", admin.get().getName());
    }

    @Test
    void findByEmailReturnsEmptyForUnknownEmail() throws Exception {
        Assertions.assertTrue(userDao.findByEmail("nobody@example.com").isEmpty());
    }

    @Test
    void emailExistsReflectsPresence() throws Exception {
        Assertions.assertTrue(userDao.emailExists("seller@sharveshmart.com"));
        Assertions.assertFalse(userDao.emailExists("missing@example.com"));
    }

    @Test
    void countIncludesSeededAccounts() throws Exception {
        Assertions.assertEquals(3L, userDao.count());
    }

    private static Reader script(String resource) throws Exception {
        InputStream stream = UserDaoTest.class.getClassLoader().getResourceAsStream(resource);
        Assertions.assertNotNull(stream, "Missing classpath resource: " + resource);
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }
}
