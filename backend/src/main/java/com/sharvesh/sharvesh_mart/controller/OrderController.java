package com.sharvesh.sharvesh_mart.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sharvesh.sharvesh_mart.dto.CartSummary;
import com.sharvesh.sharvesh_mart.dto.UserResponse;
import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.exception.InsufficientStockException;
import com.sharvesh.sharvesh_mart.exception.ValidationException;
import com.sharvesh.sharvesh_mart.model.Order;
import com.sharvesh.sharvesh_mart.model.Role;
import com.sharvesh.sharvesh_mart.service.CartService;
import com.sharvesh.sharvesh_mart.service.OrderService;
import com.sharvesh.sharvesh_mart.util.SessionUtil;

/**
 * Thin controller for checkout and order history: the mock payment
 * confirmation (F5), the buyer's order history and order detail (F6), and the
 * seller's received-orders view (F6).
 */
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    /**
     * Creates the controller with the order and cart services.
     *
     * @param orderService the order service
     * @param cartService  the cart service
     */
    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    /**
     * Renders the checkout page with the cart summary and the mock payment
     * confirmation form.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the checkout view, or a redirect to the cart if it is empty
     * @throws DaoException if persistence fails
     */
    public String checkout(HttpServletRequest request, HttpServletResponse response) throws DaoException {
        UserResponse user = SessionUtil.getUser(request);
        CartSummary cart = cartService.getCartSummary(user.getId());
        if (cart.getLines().isEmpty()) {
            return "redirect:/cart";
        }
        request.setAttribute("cart", cart);
        request.setAttribute("cartCount", cart.getLines().size());
        return "checkout.jsp";
    }

    /**
     * Confirms the mock payment and places the order from the buyer's cart.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a redirect to the order confirmation
     * @throws DaoException if persistence fails
     */
    public String place(HttpServletRequest request, HttpServletResponse response)
            throws DaoException, IOException {
        UserResponse user = SessionUtil.getUser(request);
        try {
            Order order = orderService.placeOrder(user.getId());
            return "redirect:/orders/" + order.getId();
        } catch (InsufficientStockException e) {
            return "redirect:/cart?error=" + "Some items are out of stock.";
        } catch (ValidationException e) {
            String message = java.net.URLEncoder.encode(
                    e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            return "redirect:/cart?error=" + message;
        }
    }

    /**
     * Renders the buyer's order history.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the orders view
     * @throws DaoException if persistence fails
     */
    public String orders(HttpServletRequest request, HttpServletResponse response) throws DaoException {
        UserResponse user = SessionUtil.getUser(request);
        request.setAttribute("orders", orderService.getBuyerOrders(user.getId()));
        request.setAttribute("cartCount", cartService.getCount(user.getId()));
        return "orders.jsp";
    }

    /**
     * Renders an order's detail / payment confirmation page.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the order detail view, or {@code null} after a 404
     * @throws DaoException if persistence fails
     */
    public String order(HttpServletRequest request, HttpServletResponse response)
            throws DaoException, IOException {
        UserResponse user = SessionUtil.getUser(request);
        Integer id = pathParamId(request);
        Optional<OrderService.OrderDetail> detail = id == null ? Optional.empty()
                : orderService.getOrderDetail(id, user.getId(), user.getRole());
        if (detail.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        request.setAttribute("order", detail.get().order());
        request.setAttribute("items", detail.get().items());
        return "order-confirmation.jsp";
    }

    /**
     * Renders the seller's received-orders view.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the seller orders view, or {@code null} after a 403
     * @throws DaoException if persistence fails
     */
    public String sellerOrders(HttpServletRequest request, HttpServletResponse response)
            throws DaoException, IOException {
        UserResponse user = SessionUtil.getUser(request);
        Role role = user.getRole();
        if (!Role.SELLER.equals(role) && !Role.ADMIN.equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        request.setAttribute("orders", orderService.getSellerOrders(user.getId()));
        request.setAttribute("cartCount", cartService.getCount(user.getId()));
        return "seller-orders.jsp";
    }

    private Integer pathParamId(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) request.getAttribute(Router.PATH_PARAMS_KEY);
        if (params == null) {
            return null;
        }
        String value = params.get("id");
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
