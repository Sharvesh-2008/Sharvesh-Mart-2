package com.sharvesh.sharvesh_mart.service;

import java.util.List;
import java.util.Optional;

import com.sharvesh.sharvesh_mart.dao.ProductDao;
import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.model.Product;

/**
 * Catalog business rules: browsing, keyword search and category filtering
 * (requirement F3). Depends on the {@link ProductDao} interface only.
 */
public class ProductService {

    private final ProductDao productDao;

    /**
     * Creates a service backed by the given product DAO.
     *
     * @param productDao the product data access object
     */
    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    /**
     * Searches the catalog. Blank criteria are ignored.
     *
     * @param keyword  the free-text keyword, or blank
     * @param category the exact category, or blank
     * @return the matching products
     * @throws DaoException if persistence fails
     */
    public List<Product> search(String keyword, String category) throws DaoException {
        return productDao.search(normalize(keyword), normalize(category));
    }

    /**
     * Returns the distinct categories in the catalog for the filter dropdown.
     *
     * @return the categories
     * @throws DaoException if persistence fails
     */
    public List<String> listCategories() throws DaoException {
        return productDao.findCategories();
    }

    /**
     * Finds a product by id.
     *
     * @param id the product id
     * @return the product, or {@link Optional#empty()} if not found
     * @throws DaoException if persistence fails
     */
    public Optional<Product> getById(int id) throws DaoException {
        return productDao.findById(id);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
