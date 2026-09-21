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

import com.sharvesh.sharvesh_mart.dao.CartDao;
import com.sharvesh.sharvesh_mart.dao.ProductDao;
import com.sharvesh.sharvesh_mart.dto.CartSummary;
import com.sharvesh.sharvesh_mart.exception.ValidationException;
import com.sharvesh.sharvesh_mart.model.CartItem;
import com.sharvesh.sharvesh_mart.model.Product;

/**
 * Unit tests for the cart business rules with the DAOs mocked.
 */
class CartServiceTest {

    private final CartDao cartDao = mock(CartDao.class);
    private final ProductDao productDao = mock(ProductDao.class);
    private final CartService cartService = new CartService(cartDao, productDao);

    @Test
    void getCartSummaryBuildsLinesWithRunningTotal() throws Exception {
        when(cartDao.findByUser(5)).thenReturn(List.of(
                cartItem(11, 5, 1, 2),
                cartItem(12, 5, 2, 1)));
        when(productDao.findById(1)).thenReturn(Optional.of(product(1, "Mouse", "24.50")));
        when(productDao.findById(2)).thenReturn(Optional.of(product(2, "Bottle", "15.00")));

        CartSummary summary = cartService.getCartSummary(5);

        Assertions.assertEquals(2, summary.getLines().size());
        Assertions.assertEquals(new BigDecimal("49.00"), summary.getLines().get(0).getLineTotal());
        Assertions.assertEquals(new BigDecimal("64.00"), summary.getTotal());
    }

    @Test
    void getCartSummaryReturnsEmptyForEmptyCart() throws Exception {
        when(cartDao.findByUser(5)).thenReturn(List.of());

        CartSummary summary = cartService.getCartSummary(5);

        Assertions.assertTrue(summary.getLines().isEmpty());
        Assertions.assertEquals(BigDecimal.ZERO, summary.getTotal());
    }

    @Test
    void addItemInsertsNewLine() throws Exception {
        when(cartDao.findItem(5, 9)).thenReturn(Optional.empty());
        when(productDao.findById(9)).thenReturn(Optional.of(product(9, "Keyboard", "89.99", 20)));

        cartService.addItem(5, 9, 3);

        verify(cartDao).add(any(CartItem.class));
        verify(cartDao, never()).updateQuantity(anyInt(), anyInt());
    }

    @Test
    void addItemMergesWithExistingLine() throws Exception {
        when(cartDao.findItem(5, 9)).thenReturn(Optional.of(cartItem(11, 5, 9, 2)));
        when(productDao.findById(9)).thenReturn(Optional.of(product(9, "Keyboard", "89.99", 20)));

        cartService.addItem(5, 9, 3);

        verify(cartDao).updateQuantity(11, 5);
        verify(cartDao, never()).add(any(CartItem.class));
    }

    @Test
    void addItemRejectsQuantityOutsideRange() throws Exception {
        when(productDao.findById(9)).thenReturn(Optional.of(product(9, "Keyboard", "89.99", 20)));

        Assertions.assertThrows(ValidationException.class, () -> cartService.addItem(5, 9, 0));
        Assertions.assertThrows(ValidationException.class, () -> cartService.addItem(5, 9, 100));
        verify(cartDao, never()).add(any(CartItem.class));
    }

    @Test
    void addItemRejectsQuantityAboveStock() throws Exception {
        when(cartDao.findItem(5, 9)).thenReturn(Optional.empty());
        when(productDao.findById(9)).thenReturn(Optional.of(product(9, "Keyboard", "89.99", 2)));

        Assertions.assertThrows(ValidationException.class, () -> cartService.addItem(5, 9, 3));

        verify(cartDao, never()).add(any(CartItem.class));
    }

    @Test
    void updateQuantityVerifiesOwnershipBeforeWriting() throws Exception {
        when(cartDao.findById(11)).thenReturn(Optional.of(cartItem(11, 5, 9, 2)));
        when(productDao.findById(9)).thenReturn(Optional.of(product(9, "Keyboard", "89.99", 20)));

        cartService.updateQuantity(5, 11, 4);

        verify(cartDao).updateQuantity(11, 4);
    }

    @Test
    void updateQuantityRejectsAnotherUsersLine() throws Exception {
        when(cartDao.findById(11)).thenReturn(Optional.of(cartItem(11, 99, 9, 2)));

        Assertions.assertThrows(ValidationException.class, () -> cartService.updateQuantity(5, 11, 4));

        verify(cartDao, never()).updateQuantity(anyInt(), anyInt());
    }

    @Test
    void updateQuantityRejectsQuantityAboveStock() throws Exception {
        when(cartDao.findById(11)).thenReturn(Optional.of(cartItem(11, 5, 9, 2)));
        when(productDao.findById(9)).thenReturn(Optional.of(product(9, "Keyboard", "89.99", 1)));

        Assertions.assertThrows(ValidationException.class, () -> cartService.updateQuantity(5, 11, 4));

        verify(cartDao, never()).updateQuantity(anyInt(), anyInt());
    }

    @Test
    void removeItemVerifiesOwnershipBeforeRemoving() throws Exception {
        when(cartDao.findById(11)).thenReturn(Optional.of(cartItem(11, 5, 9, 2)));

        cartService.removeItem(5, 11);

        verify(cartDao).remove(11);
    }

    @Test
    void removeItemRejectsAnotherUsersLine() throws Exception {
        when(cartDao.findById(11)).thenReturn(Optional.of(cartItem(11, 99, 9, 2)));

        Assertions.assertThrows(ValidationException.class, () -> cartService.removeItem(5, 11));

        verify(cartDao, never()).remove(anyInt());
    }

    @Test
    void getCountReturnsDaoValue() throws Exception {
        when(cartDao.count(eq(5))).thenReturn(3);

        Assertions.assertEquals(3, cartService.getCount(5));
    }

    private Product product(int id, String name, String price, int stock) {
        Product product = product(id, name, price);
        product.setStockQty(stock);
        return product;
    }

    private Product product(int id, String name, String price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        return product;
    }

    private CartItem cartItem(int id, int userId, int productId, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }
}
