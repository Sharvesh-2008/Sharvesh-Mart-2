package com.saranya.saranyamart.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.saranya.saranyamart.dto.CartSummary;
import com.saranya.saranyamart.dto.UserResponse;
import com.saranya.saranyamart.exception.DaoException;
import com.saranya.saranyamart.exception.ValidationException;
import com.saranya.saranyamart.service.CartService;
import com.saranya.saranyamart.util.SessionUtil;

/**
 * Thin controller for the shopping cart (requirement F4): view, add, update
 * quantity and remove. All writes redirect after POST to avoid resubmission.
 */
public class CartController {

    private static final int DEFAULT_QUANTITY = 1;

    private final CartService cartService;

    /**
     * Creates the controller with the cart service.
     *
     * @param cartService the cart service
     */
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Renders the cart with line items and the running total.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the cart view
     * @throws DaoException if persistence fails
     */
    public String cart(HttpServletRequest request, HttpServletResponse response) throws DaoException {
        UserResponse user = SessionUtil.getUser(request);
        CartSummary summary = cartService.getCartSummary(user.getId());
        request.setAttribute("cart", summary);
        request.setAttribute("cartCount", summary.getLines().size());
        return "cart.jsp";
    }

    /**
     * Adds a product to the cart.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a redirect to the cart (or back to the product on error)
     * @throws DaoException if persistence fails
     */
    public String add(HttpServletRequest request, HttpServletResponse response) throws DaoException {
        int productId = parsePositive(request.getParameter("productId"));
        int quantity = parsePositive(request.getParameter("quantity"));
        if (quantity <= 0) {
            quantity = DEFAULT_QUANTITY;
        }
        if (productId <= 0) {
            return "redirect:/products?error=" + encode("Product not found.");
        }
        UserResponse user = SessionUtil.getUser(request);
        try {
            cartService.addItem(user.getId(), productId, quantity);
            return "redirect:/cart?added=1";
        } catch (ValidationException e) {
            return "redirect:/products/" + productId + "?error=" + encode(e.getMessage());
        }
    }

    /**
     * Updates the quantity of a cart line.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a redirect to the cart
     * @throws DaoException if persistence fails
     */
    public String update(HttpServletRequest request, HttpServletResponse response) throws DaoException {
        int itemId = parsePositive(request.getParameter("itemId"));
        int quantity = parsePositive(request.getParameter("quantity"));
        UserResponse user = SessionUtil.getUser(request);
        try {
            cartService.updateQuantity(user.getId(), itemId, quantity);
            return "redirect:/cart";
        } catch (ValidationException e) {
            return "redirect:/cart?error=" + encode(e.getMessage());
        }
    }

    /**
     * Removes a cart line.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a redirect to the cart
     * @throws DaoException if persistence fails
     */
    public String remove(HttpServletRequest request, HttpServletResponse response) throws DaoException {
        int itemId = parsePositive(request.getParameter("itemId"));
        UserResponse user = SessionUtil.getUser(request);
        try {
            cartService.removeItem(user.getId(), itemId);
        } catch (ValidationException e) {
            // The line is gone or does not belong to the user; treat as a no-op.
        }
        return "redirect:/cart";
    }

    private int parsePositive(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value < 0 ? 0 : value;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
