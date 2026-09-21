package com.sharvesh.sharvesh_mart.model;

/**
 * Entity representing a row of a buyer's shopping cart (mirrors the
 * {@code cart_items} table).
 */
public class CartItem {

    private Integer id;
    private Integer userId;
    private Integer productId;
    private int quantity;

    /**
     * Creates an empty cart item. Fields are populated via setters by the DAO
     * or service.
     */
    public CartItem() {
        // Default constructor for JDBC row mapping.
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
