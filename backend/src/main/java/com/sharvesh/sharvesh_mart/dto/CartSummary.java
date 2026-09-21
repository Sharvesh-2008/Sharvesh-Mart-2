package com.sharvesh.sharvesh_mart.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Snapshot of a user's cart: the display lines plus the running grand total
 * (requirement F4).
 */
public class CartSummary {

    private final List<CartLine> lines;
    private final BigDecimal total;

    /**
     * Creates a cart summary.
     *
     * @param lines the cart display lines
     * @param total the running total of all lines
     */
    public CartSummary(List<CartLine> lines, BigDecimal total) {
        this.lines = lines;
        this.total = total;
    }

    public List<CartLine> getLines() {
        return lines;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
