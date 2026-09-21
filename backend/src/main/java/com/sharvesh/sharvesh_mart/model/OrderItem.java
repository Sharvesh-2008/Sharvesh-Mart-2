package com.sharvesh.sharvesh_mart.model;

import java.math.BigDecimal;

/**
 * Entity representing one line of a purchase order (mirrors the
 * {@code order_items} table). {@code productName} is populated by the DAO
 * from a join for display and is not a persisted column.
 */
public class OrderItem {

    private Integer id;
    private Integer orderId;
    private Integer productId;
    private int quantity;
    private BigDecimal unitPrice;
    private String productName;

    /**
     * Creates an empty order item. Fields are populated via setters by the DAO
     * or service.
     */
    public OrderItem() {
        // Default constructor for JDBC row mapping.
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
