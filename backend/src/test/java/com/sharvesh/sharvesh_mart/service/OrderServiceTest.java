package com.sharvesh.sharvesh_mart.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.sharvesh.sharvesh_mart.dao.CartDao;
import com.sharvesh.sharvesh_mart.dao.OrderDao;
import com.sharvesh.sharvesh_mart.exception.ValidationException;
import com.sharvesh.sharvesh_mart.model.CartItem;
import com.sharvesh.sharvesh_mart.model.Order;
import com.sharvesh.sharvesh_mart.model.OrderItem;
import com.sharvesh.sharvesh_mart.model.OrderStatus;
import com.sharvesh.sharvesh_mart.model.Role;
import com.sharvesh.sharvesh_mart.service.OrderService.OrderDetail;

/**
 * Unit tests for the order placement and history rules with the DAOs mocked.
 */
class OrderServiceTest {

    private final CartDao cartDao = mock(CartDao.class);
    private final OrderDao orderDao = mock(OrderDao.class);
    private final OrderService orderService = new OrderService(cartDao, orderDao);

    @Test
    void placeOrderBuildsItemsFromCartAndClearsIt() throws Exception {
        when(cartDao.findByUser(5)).thenReturn(List.of(
                cartItem(1, 5, 10, 2),
                cartItem(2, 5, 11, 1)));
        when(orderDao.placeOrder(anyInt(), any())).thenReturn(order(9, 5));

        Order placed = orderService.placeOrder(5);

        Assertions.assertEquals(9, placed.getId());
        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderDao).placeOrder(eq(5), itemsCaptor.capture());
        Assertions.assertEquals(2, itemsCaptor.getValue().size());
        Assertions.assertEquals(10, itemsCaptor.getValue().get(0).getProductId());
        Assertions.assertEquals(2, itemsCaptor.getValue().get(0).getQuantity());
        verify(cartDao).clear(5);
    }

    @Test
    void placeOrderRejectsEmptyCart() throws Exception {
        when(cartDao.findByUser(5)).thenReturn(List.of());

        Assertions.assertThrows(ValidationException.class, () -> orderService.placeOrder(5));

        verify(orderDao, never()).placeOrder(anyInt(), any());
        verify(cartDao, never()).clear(anyInt());
    }

    @Test
    void buyerCanViewOwnOrder() throws Exception {
        when(orderDao.findById(9)).thenReturn(Optional.of(order(9, 5)));
        when(orderDao.findItemsByOrder(9)).thenReturn(List.of(orderItem(9, 10, 2, "Keyboard")));

        Optional<OrderDetail> detail = orderService.getOrderDetail(9, 5, Role.BUYER);

        Assertions.assertTrue(detail.isPresent());
        Assertions.assertEquals(1, detail.get().items().size());
        Assertions.assertEquals("Keyboard", detail.get().items().get(0).getProductName());
    }

    @Test
    void anotherBuyerCannotViewOrder() throws Exception {
        when(orderDao.findById(9)).thenReturn(Optional.of(order(9, 5)));

        Optional<OrderDetail> detail = orderService.getOrderDetail(9, 7, Role.BUYER);

        Assertions.assertTrue(detail.isEmpty());
        verify(orderDao, never()).findItemsByOrder(anyInt());
    }

    @Test
    void adminCanViewAnyOrder() throws Exception {
        when(orderDao.findById(9)).thenReturn(Optional.of(order(9, 5)));
        when(orderDao.findItemsByOrder(9)).thenReturn(List.of());

        Optional<OrderDetail> detail = orderService.getOrderDetail(9, 1, Role.ADMIN);

        Assertions.assertTrue(detail.isPresent());
    }

    @Test
    void sellerCanViewOrderContainingOwnProduct() throws Exception {
        when(orderDao.findById(9)).thenReturn(Optional.of(order(9, 5)));
        when(orderDao.findBySeller(2)).thenReturn(List.of(order(9, 5), order(3, 5)));
        when(orderDao.findItemsByOrder(9)).thenReturn(List.of());

        Optional<OrderDetail> detail = orderService.getOrderDetail(9, 2, Role.SELLER);

        Assertions.assertTrue(detail.isPresent());
        verify(orderDao).findBySeller(2);
    }

    @Test
    void sellerCannotViewUnrelatedOrder() throws Exception {
        when(orderDao.findById(9)).thenReturn(Optional.of(order(9, 5)));
        when(orderDao.findBySeller(2)).thenReturn(List.of(order(3, 5)));

        Optional<OrderDetail> detail = orderService.getOrderDetail(9, 2, Role.SELLER);

        Assertions.assertTrue(detail.isEmpty());
    }

    @Test
    void missingOrderReturnsEmpty() throws Exception {
        when(orderDao.findById(9)).thenReturn(Optional.empty());

        Assertions.assertTrue(orderService.getOrderDetail(9, 5, Role.BUYER).isEmpty());
    }

    @Test
    void getBuyerOrdersDelegatesToDao() throws Exception {
        when(orderDao.findByBuyer(5)).thenReturn(List.of(order(9, 5)));

        Assertions.assertEquals(1, orderService.getBuyerOrders(5).size());
    }

    @Test
    void getSellerOrdersDelegatesToDao() throws Exception {
        when(orderDao.findBySeller(2)).thenReturn(List.of(order(9, 5)));

        Assertions.assertEquals(1, orderService.getSellerOrders(2).size());
    }

    private Order order(int id, int buyerId) {
        Order order = new Order();
        order.setId(id);
        order.setBuyerId(buyerId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(new BigDecimal("49.00"));
        return order;
    }

    private CartItem cartItem(int id, int userId, int productId, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private OrderItem orderItem(int orderId, int productId, int quantity, String name) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setProductName(name);
        return item;
    }
}
