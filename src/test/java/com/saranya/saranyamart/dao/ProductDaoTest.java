package com.saranya.saranyamart.dao;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.saranya.saranyamart.model.Product;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DAO tests for the product catalog against an embedded H2 instance
 * initialized from the checked-in schema and seed scripts.
 */
class ProductDaoTest {

    private static final String TEST_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    private static HikariDataSource dataSource;
    private static ProductDao productDao;

    @BeforeAll
    static void setUpPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(TEST_URL);
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new HikariDataSource(config);
        productDao = new ProductDaoImpl(dataSource);
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
    void findByIdFindsSeededProduct() throws Exception {
        Optional<Product> product = productDao.findById(1);

        Assertions.assertTrue(product.isPresent());
        Assertions.assertEquals("Mechanical Keyboard", product.get().getName());
        Assertions.assertEquals(2, product.get().getSellerId());
    }

    @Test
    void findByIdReturnsEmptyForUnknownProduct() throws Exception {
        Assertions.assertTrue(productDao.findById(999).isEmpty());
    }

    @Test
    void searchMatchesKeywordIgnoringCase() throws Exception {
        List<Product> products = productDao.search("KEYBOARD", null);

        Assertions.assertEquals(1, products.size());
        Assertions.assertEquals("Mechanical Keyboard", products.get(0).getName());
    }

    @Test
    void searchFiltersByCategory() throws Exception {
        List<Product> products = productDao.search(null, "electronics");

        Assertions.assertEquals(2, products.size());
    }

    @Test
    void searchWithNoCriteriaReturnsAll() throws Exception {
        Assertions.assertEquals(3, productDao.search(null, null).size());
        Assertions.assertEquals(3, productDao.search("", "").size());
    }

    @Test
    void findCategoriesReturnsDistinctSortedValues() throws Exception {
        List<String> categories = productDao.findCategories();

        Assertions.assertEquals(List.of("Electronics", "Home & Kitchen"), categories);
    }

    private static Reader script(String resource) throws Exception {
        InputStream stream = ProductDaoTest.class.getClassLoader().getResourceAsStream(resource);
        Assertions.assertNotNull(stream, "Missing classpath resource: " + resource);
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }
}
