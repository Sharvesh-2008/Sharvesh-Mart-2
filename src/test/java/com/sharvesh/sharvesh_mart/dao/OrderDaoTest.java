package com.sharvesh.sharvesh_mart.dao;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.exception.InsufficientStockException;
import com.sharvesh.sharvesh_mart.model.Order;
import com.sharvesh.sharvesh_mart.model.OrderItem;
import com.sharvesh.sharvesh_mart.model.OrderStatus;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DAO tests for transactional order placement and history queries against an
 * embedded H2 instance initialized from the checked-in schema and seed scripts.
 */
class OrderDaoTest {

    private static final String TEST_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final int BUYER_ID = 3;
    private static final int SELLER_ID = 2;

    private static HikariDataSource dataSource;
    private static OrderDao orderDao;
    private static ProductDao productDao;

    @BeforeAll
    static void setUpPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(TEST_URL);
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new HikariDataSource(config);
        orderDao = new OrderDaoImpl(dataSource);
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
    void placeOrderCreatesConfirmedOrderAndDecrementsStock() throws Exception {
        Order order = orderDao.placeOrder(BUYER_ID, items(line(1, 2)));

        Assertions.assertNotNull(order.getId());
        Assertions.assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        Assertions.assertEquals(new BigDecimal("179.98"), order.getTotalAmount());

        Assertions.assertEquals(23, productDao.findById(1).orElseThrow().getStockQty());
        Assertions.assertEquals(1, orderDao.findItemsByOrder(order.getId()).size());
        Assertions.assertEquals("Mechanical Keyboard",
                orderDao.findItemsByOrder(order.getId()).get(0).getProductName());
    }

    @Test
    void placeOrderComputesTotalAcrossLines() throws Exception {
        List<OrderItem> items = new ArrayList<>();
        items.add(line(1, 2));
        items.add(line(2, 1));

        Order order = orderDao.placeOrder(BUYER_ID, items);

        Assertions.assertEquals(new BigDecimal("204.48"), order.getTotalAmount());
        Assertions.assertEquals(59, productDao.findById(2).orElseThrow().getStockQty());
    }

    @Test
    void placeOrderRollsBackEverythingOnInsufficientStock() throws Exception {
        List<OrderItem> items = new ArrayList<>();
        items.add(line(1, 2));
        items.add(line(2, 1000));

        Assertions.assertThrows(InsufficientStockException.class, () -> orderDao.placeOrder(BUYER_ID, items));

        Assertions.assertEquals(25, productDao.findById(1).orElseThrow().getStockQty());
        Assertions.assertEquals(60, productDao.findById(2).orElseThrow().getStockQty());
        Assertions.assertTrue(orderDao.findByBuyer(BUYER_ID).isEmpty());
    }

    @Test
    void placeOrderRejectsEmptyItemList() {
        Assertions.assertThrows(DaoException.class, () -> orderDao.placeOrder(BUYER_ID, List.of()));
    }

    @Test
    void findByBuyerReturnsPlacedOrdersNewestFirst() throws Exception {
        Order first = orderDao.placeOrder(BUYER_ID, items(line(1, 1)));

        List<Order> orders = orderDao.findByBuyer(BUYER_ID);

        Assertions.assertEquals(1, orders.size());
        Assertions.assertEquals(first.getId(), orders.get(0).getId());
    }

    @Test
    void findBySellerFindsOrdersContainingOwnProduct() throws Exception {
        Order order = orderDao.placeOrder(BUYER_ID, items(line(1, 2)));

        List<Order> orders = orderDao.findBySeller(SELLER_ID);

        Assertions.assertTrue(orders.stream().anyMatch(o -> o.getId().equals(order.getId())));
    }

    @Test
    void findByIdReturnsEmptyForUnknownOrder() throws Exception {
        Assertions.assertTrue(orderDao.findById(999).isEmpty());
    }

    private OrderItem line(int productId, int quantity) {
        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private List<OrderItem> items(OrderItem... lines) {
        List<OrderItem> result = new ArrayList<>();
        for (OrderItem line : lines) {
            result.add(line);
        }
        return result;
    }

    private static Reader script(String resource) throws Exception {
        InputStream stream = OrderDaoTest.class.getClassLoader().getResourceAsStream(resource);
        Assertions.assertNotNull(stream, "Missing classpath resource: " + resource);
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }
}
