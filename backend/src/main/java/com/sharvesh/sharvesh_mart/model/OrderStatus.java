package com.sharvesh.sharvesh_mart.model;

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
