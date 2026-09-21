package com.sharvesh.sharvesh_mart.dao;

import javax.sql.DataSource;

/**
 * Central factory for DAO instances (spec Section 12 design patterns). The
 * data source is owned by the {@code ServletContextListener} and injected here.
 */
public class DaoFactory {

    private final DataSource dataSource;

    /**
     * Creates a factory bound to a connection pool.
     *
     * @param dataSource the pooled data source
     */
    public DaoFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Provides a {@link UserDao} implementation.
     *
     * @return the user DAO
     */
    public UserDao getUserDao() {
        return new UserDaoImpl(dataSource);
    }

    /**
     * Provides a {@link ProductDao} implementation.
     *
     * @return the product DAO
     */
    public ProductDao getProductDao() {
        return new ProductDaoImpl(dataSource);
    }

    /**
     * Provides a {@link CartDao} implementation.
     *
     * @return the cart DAO
     */
    public CartDao getCartDao() {
        return new CartDaoImpl(dataSource);
    }

    /**
     * Provides an {@link OrderDao} implementation.
     *
     * @return the order DAO
     */
    public OrderDao getOrderDao() {
        return new OrderDaoImpl(dataSource);
    }
}
