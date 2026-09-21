package com.sharvesh.sharvesh_mart.dto;

import java.math.BigDecimal;

/**
 * A single display line of the shopping cart: the cart row joined with its
 * product snapshot plus the computed line total.
 */
public class CartLine {

    private final Integer cartItemId;
    private final Integer productId;
    private final String productName;
    private final String category;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final BigDecimal lineTotal;

    /**
     * Creates a cart display line.
     *
     * @param cartItemId  the cart row id
     * @param productId   the product id
     * @param productName the product name
     * @param category    the product category
     * @param unitPrice   the product price
     * @param quantity    the requested quantity
     * @param lineTotal   quantity x unit price
     */
    public CartLine(Integer cartItemId, Integer productId, String productName, String category,
                    BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public Integer getCartItemId() {
        return cartItemId;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
