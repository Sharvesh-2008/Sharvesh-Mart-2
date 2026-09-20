package com.saranya.saranyamart.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.saranya.saranyamart.dao.CartDao;
import com.saranya.saranyamart.dao.ProductDao;
import com.saranya.saranyamart.dto.CartLine;
import com.saranya.saranyamart.dto.CartSummary;
import com.saranya.saranyamart.exception.DaoException;
import com.saranya.saranyamart.exception.ValidationException;
import com.saranya.saranyamart.model.CartItem;
import com.saranya.saranyamart.model.Product;

/**
 * Cart business rules (requirement F4): add, update, remove and the running
 * total. The cart is persisted in the {@code cart_items} table, shared per
 * user. All money math uses {@link BigDecimal}.
 */
public class CartService {

    private static final int MAX_QUANTITY_PER_LINE = 99;

    private final CartDao cartDao;
    private final ProductDao productDao;

    /**
     * Creates a service backed by the given DAOs.
     *
     * @param cartDao    the cart data access object
     * @param productDao the product data access object
     */
    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    /**
     * Loads a user's cart with product details and the running total.
     *
     * @param userId the user id
     * @return the cart summary
     * @throws DaoException if persistence fails
     */
    public CartSummary getCartSummary(int userId) throws DaoException {
        List<CartLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartDao.findByUser(userId)) {
            Optional<Product> product = productDao.findById(item.getProductId());
            if (product.isEmpty()) {
                continue;
            }
            Product catalogProduct = product.get();
            BigDecimal lineTotal = catalogProduct.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            lines.add(new CartLine(item.getId(), catalogProduct.getId(), catalogProduct.getName(),
                    catalogProduct.getCategory(), catalogProduct.getPrice(), item.getQuantity(), lineTotal));
            total = total.add(lineTotal);
        }
        return new CartSummary(lines, total);
    }

    /**
     * Counts the lines in a user's cart (used for the nav badge).
     *
     * @param userId the user id
     * @return the number of lines
     * @throws DaoException if persistence fails
     */
    public int getCount(int userId) throws DaoException {
        return cartDao.count(userId);
    }

    /**
     * Adds a quantity of a product to the cart, merging with an existing line
     * if the product is already present. Validates existence, quantity and
     * stock before any write.
     *
     * @param userId    the user id
     * @param productId the product to add
     * @param quantity  the quantity to add
     * @throws ValidationException if the request is invalid or stock is insufficient
     * @throws DaoException        if persistence fails
     */
    public void addItem(int userId, int productId, int quantity)
            throws ValidationException, DaoException {
        validateQuantity(quantity);
        Product product = requireProduct(productId);
        Optional<CartItem> existing = cartDao.findItem(userId, productId);
        int resultingQuantity = existing.map(CartItem::getQuantity).orElse(0) + quantity;
        if (resultingQuantity > product.getStockQty()) {
            throw new ValidationException("quantity", "Only " + product.getStockQty() + " in stock.");
        }
        if (existing.isPresent()) {
            cartDao.updateQuantity(existing.get().getId(), resultingQuantity);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            cartDao.add(item);
        }
    }

    /**
     * Updates the quantity of an existing cart line. Verifies the line belongs
     * to the user (ownership rule) before writing.
     *
     * @param userId     the user id
     * @param cartItemId the cart line to change
     * @param quantity   the resulting quantity
     * @throws ValidationException if the request is invalid or stock is insufficient
     * @throws DaoException        if persistence fails
     */
    public void updateQuantity(int userId, int cartItemId, int quantity)
            throws ValidationException, DaoException {
        validateQuantity(quantity);
        CartItem item = requireOwnedItem(userId, cartItemId);
        Product product = requireProduct(item.getProductId());
        if (quantity > product.getStockQty()) {
            throw new ValidationException("quantity", "Only " + product.getStockQty() + " in stock.");
        }
        cartDao.updateQuantity(item.getId(), quantity);
    }

    /**
     * Removes a cart line after verifying it belongs to the user.
     *
     * @param userId     the user id
     * @param cartItemId the cart line to remove
     * @throws ValidationException if the line is not found or not owned
     * @throws DaoException        if persistence fails
     */
    public void removeItem(int userId, int cartItemId) throws ValidationException, DaoException {
        requireOwnedItem(userId, cartItemId);
        cartDao.remove(cartItemId);
    }

    private void validateQuantity(int quantity) throws ValidationException {
        if (quantity < 1 || quantity > MAX_QUANTITY_PER_LINE) {
            throw new ValidationException("quantity",
                    "Quantity must be between 1 and " + MAX_QUANTITY_PER_LINE + ".");
        }
    }

    private Product requireProduct(int productId) throws DaoException, ValidationException {
        return productDao.findById(productId)
                .orElseThrow(() -> new ValidationException("product", "Product not found."));
    }

    private CartItem requireOwnedItem(int userId, int cartItemId) throws DaoException, ValidationException {
        CartItem item = cartDao.findById(cartItemId)
                .orElseThrow(() -> new ValidationException("item", "Cart item not found."));
        if (item.getUserId() != userId) {
            throw new ValidationException("item", "Cart item not found.");
        }
        return item;
    }
}
