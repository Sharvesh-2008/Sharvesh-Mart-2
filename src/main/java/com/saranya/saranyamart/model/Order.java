package com.saranya.saranyamart.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a purchase order (mirrors the {@code orders} table).
 */
public class Order {

    private Integer id;
    private Integer buyerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    /**
     * Creates an empty order. Fields are populated via setters by the DAO or service.
     */
    public Order() {
        // Default constructor for JDBC row mapping.
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
