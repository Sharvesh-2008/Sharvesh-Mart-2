package com.saranya.saranyamart.model;

/**
 * Lifecycle states of an order (mirrors the {@code orders.status} column).
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
