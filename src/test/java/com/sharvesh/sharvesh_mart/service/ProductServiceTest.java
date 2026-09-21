package com.sharvesh.sharvesh_mart.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.sharvesh.sharvesh_mart.dao.ProductDao;
import com.sharvesh.sharvesh_mart.model.Product;

/**
 * Unit tests for the catalog search rules with the DAO mocked.
 */
class ProductServiceTest {

    private final ProductDao productDao = mock(ProductDao.class);
    private final ProductService productService = new ProductService(productDao);

    @Test
    void searchTrimsAndPassesCriteriaToDao() throws Exception {
        when(productDao.search("keyboard", "electronics")).thenReturn(List.of(newProduct()));

        List<Product> products = productService.search("  keyboard ", " electronics ");

        Assertions.assertEquals(1, products.size());
        verify(productDao).search("keyboard", "electronics");
    }

    @Test
    void searchWithBlankCriteriaPassesBlanksToDao() throws Exception {
        when(productDao.search("", "")).thenReturn(List.of());

        List<Product> products = productService.search(null, null);

        Assertions.assertTrue(products.isEmpty());
        verify(productDao).search("", "");
    }

    @Test
    void listCategoriesDelegatesToDao() throws Exception {
        when(productDao.findCategories()).thenReturn(List.of("Electronics", "Home & Kitchen"));

        List<String> categories = productService.listCategories();

        Assertions.assertEquals(2, categories.size());
        verify(productDao).findCategories();
    }

    @Test
    void getByIdReturnsProductWhenFound() throws Exception {
        when(productDao.findById(7)).thenReturn(Optional.of(newProduct()));

        Optional<Product> product = productService.getById(7);

        Assertions.assertTrue(product.isPresent());
        Assertions.assertEquals("Keyboard", product.get().getName());
    }

    @Test
    void getByIdReturnsEmptyWhenMissing() throws Exception {
        when(productDao.findById(99)).thenReturn(Optional.empty());

        Assertions.assertTrue(productService.getById(99).isEmpty());
        verify(productDao).findById(99);
    }

    @Test
    void normalizeNeverInvokedWithBlankKeyword() throws Exception {
        when(productDao.search(anyString(), anyString())).thenReturn(List.of());

        productService.search("   ", "  ");

        verify(productDao).search("", "");
    }

    private Product newProduct() {
        Product product = new Product();
        product.setId(7);
        product.setSellerId(2);
        product.setName("Keyboard");
        return product;
    }
}
