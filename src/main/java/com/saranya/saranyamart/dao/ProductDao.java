package com.saranya.saranyamart.dao;

import java.util.List;
import java.util.Optional;

import com.saranya.saranyamart.exception.DaoException;
import com.saranya.saranyamart.model.Product;

/**
 * Data access contract for the {@code products} table. All SQL lives in the
 * JDBC implementations and every statement is a PreparedStatement.
 */
public interface ProductDao {

    /** Selected columns shared by product reads. */
    String COLUMNS = "id, seller_id, name, description, price, stock_qty, category, created_at";

    /**
     * Finds a product by primary key.
     *
     * @param id the product id
     * @return the product, or {@link Optional#empty()} if not found
     * @throws DaoException if the query fails
     */
    Optional<Product> findById(int id) throws DaoException;

    /**
     * Searches the catalog by a keyword (against name/description) and an
     * optional category. Either criterion may be blank to mean "any".
     *
     * @param keyword  the free-text keyword, or {@code null}/{@code blank}
     * @param category the exact category, or {@code null}/{@code blank}
     * @return the matching products, newest first
     * @throws DaoException if the query fails
     */
    List<Product> search(String keyword, String category) throws DaoException;

    /**
     * Returns the distinct non-empty categories present in the catalog.
     *
     * @return the categories, sorted alphabetically
     * @throws DaoException if the query fails
     */
    List<String> findCategories() throws DaoException;

    /**
     * Returns all products owned by a seller, newest first.
     *
     * @param sellerId the seller's user id
     * @return the seller's products
     * @throws DaoException if the query fails
     */
    List<Product> findBySeller(int sellerId) throws DaoException;

    /**
     * Inserts a new product and assigns the generated id and created-at
     * timestamp.
     *
     * @param product the product to persist (without an id)
     * @return the persisted product including the generated id
     * @throws DaoException if the insert fails
     */
    Product create(Product product) throws DaoException;

    /**
     * Updates the seller-editable fields of an existing product.
     *
     * @param product the product update (must carry an id)
     * @throws DaoException if the update fails
     */
    void update(Product product) throws DaoException;

    /**
     * Deletes a product by id.
     *
     * @param id the product id
     * @throws DaoException if the delete fails
     */
    void delete(int id) throws DaoException;

    /**
     * Returns whether a product is referenced by a cart line or an order line
     * and therefore must not be deleted.
     *
     * @param productId the product id
     * @return {@code true} if referenced, {@code false} otherwise
     * @throws DaoException if the query fails
     */
    boolean isReferenced(int productId) throws DaoException;
}
