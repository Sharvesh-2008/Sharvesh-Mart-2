package com.saranya.saranyamart.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.saranya.saranyamart.dao.CartDao;
import com.saranya.saranyamart.dao.OrderDao;
import com.saranya.saranyamart.exception.DaoException;
import com.saranya.saranyamart.exception.InsufficientStockException;
import com.saranya.saranyamart.exception.ValidationException;
import com.saranya.saranyamart.model.CartItem;
import com.saranya.saranyamart.model.Order;
import com.saranya.saranyamart.model.OrderItem;
import com.saranya.saranyamart.model.Role;

/**
 * Order business rules (requirements F5 and F6): placing an order from the
 * cart through the mock payment confirmation, and buyer/seller order history.
 * Depends on DAO interfaces only; all money math stays out of JDBC.
 */
public class OrderService {

    private final CartDao cartDao;
    private final OrderDao orderDao;

    /**
     * Creates a service backed by the given DAOs.
     *
     * @param cartDao  the cart data access object
     * @param orderDao the order data access object
     */
    public OrderService(CartDao cartDao, OrderDao orderDao) {
        this.cartDao = cartDao;
        this.orderDao = orderDao;
    }

    /**
     * Places an order from the buyer's current cart. The result is a
     * CONFIRMED order (the checkout's mock payment is accepted here), stock is
     * decremented transactionally, and the cart is cleared on success.
     *
     * @param buyerId the buyer's user id
     * @return the placed order
     * @throws ValidationException       if the cart is empty
     * @throws InsufficientStockException if any line cannot be fulfilled
     * @throws DaoException              if persistence fails
     */
    public Order placeOrder(int buyerId) throws ValidationException, InsufficientStockException, DaoException {
        List<CartItem> cart = cartDao.findByUser(buyerId);
        if (cart.isEmpty()) {
            throw new ValidationException("cart", "Your cart is empty.");
        }
        List<OrderItem> items = new ArrayList<>();
        for (CartItem cartItem : cart) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            items.add(orderItem);
        }
        Order placed = orderDao.placeOrder(buyerId, items);
        cartDao.clear(buyerId);
        return placed;
    }

    /**
     * Returns all orders placed by a buyer, newest first (F6 buyer view).
     *
     * @param buyerId the buyer's user id
     * @return the orders
     * @throws DaoException if persistence fails
     */
    public List<Order> getBuyerOrders(int buyerId) throws DaoException {
        return orderDao.findByBuyer(buyerId);
    }

    /**
     * Returns the orders containing products owned by a seller (F6 seller view).
     *
     * @param sellerId the seller's user id
     * @return the orders
     * @throws DaoException if persistence fails
     */
    public List<Order> getSellerOrders(int sellerId) throws DaoException {
        return orderDao.findBySeller(sellerId);
    }

    /**
     * Loads an order with its lines, enforcing access control: the buyer who
     * placed it, administrators, or a seller with a product in the order may
     * view it.
     *
     * @param orderId the order id
     * @param userId  the requesting user id
     * @param role    the requesting user's role
     * @return the order detail, or {@link Optional#empty()} if not found or
     *         not permitted
     * @throws DaoException if persistence fails
     */
    public Optional<OrderDetail> getOrderDetail(int orderId, int userId, Role role) throws DaoException {
        Optional<Order> order = orderDao.findById(orderId);
        if (order.isEmpty() || !canView(order.get(), userId, role)) {
            return Optional.empty();
        }
        return Optional.of(new OrderDetail(order.get(), orderDao.findItemsByOrder(orderId)));
    }

    private boolean canView(Order order, int userId, Role role) throws DaoException {
        if (Role.ADMIN.equals(role) || order.getBuyerId().equals(userId)) {
            return true;
        }
        if (Role.SELLER.equals(role)) {
            return orderDao.findBySeller(userId).stream()
                    .anyMatch(sellerOrder -> sellerOrder.getId().equals(order.getId()));
        }
        return false;
    }

    /**
     * An order together with its display lines.
     *
     * @param order the order header
     * @param items the order lines
     */
    public record OrderDetail(Order order, List<OrderItem> items) {
    }
}
