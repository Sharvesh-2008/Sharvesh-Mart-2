package com.sharvesh.sharvesh_mart.exception;

/**
 * Thrown when a purchase line cannot be fulfilled because the product's
 * stock quantity is lower than the requested quantity.
 */
public class InsufficientStockException extends Exception {

    private final Integer productId;
    private final int requested;

    /**
     * Creates an exception for an unfulfillable line.
     *
     * @param productId the identifier of the product with too little stock
     * @param requested the requested quantity
     */
    public InsufficientStockException(Integer productId, int requested) {
        super("Insufficient stock for the requested quantity.");
        this.productId = productId;
        this.requested = requested;
    }

    /**
     * Returns the product that ran out of stock.
     *
     * @return the product id
     */
    public Integer getProductId() {
        return productId;
    }

    /**
     * Returns the requested quantity.
     *
     * @return the requested quantity
     */
    public int getRequested() {
        return requested;
    }
}
