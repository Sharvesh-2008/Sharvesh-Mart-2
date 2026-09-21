package com.sharvesh.sharvesh_mart.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sharvesh.sharvesh_mart.dto.UserResponse;
import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.model.Product;
import com.sharvesh.sharvesh_mart.service.CartService;
import com.sharvesh.sharvesh_mart.service.ProductService;
import com.sharvesh.sharvesh_mart.util.SessionUtil;

/**
 * Thin controller for the buyer catalog: browse with keyword search and
 * category filtering, and the product detail page (requirement F3).
 */
public class CatalogController {

    private static final String PATH_PARAM_ID = "id";

    private final ProductService productService;
    private final CartService cartService;

    /**
     * Creates the controller with the product and cart services.
     *
     * @param productService the product service
     * @param cartService    the cart service (for the nav badge)
     */
    public CatalogController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    /**
     * Renders the catalog with the applied search filters.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the catalog view
     * @throws DaoException if persistence fails
     */
    public String catalog(HttpServletRequest request, HttpServletResponse response) throws DaoException {
        String keyword = request.getParameter("q");
        String category = request.getParameter("category");

        List<Product> products = productService.search(keyword, category);
        request.setAttribute("products", products);
        request.setAttribute("categories", productService.listCategories());
        request.setAttribute("q", keyword == null ? "" : keyword);
        request.setAttribute("category", category == null ? "" : category);
        UserResponse user = SessionUtil.getUser(request);
        request.setAttribute("cartCount", cartService.getCount(user.getId()));
        return "catalog.jsp";
    }

    /**
     * Renders a single product's detail page.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the product view, or {@code null} after sending a 404
     * @throws DaoException if persistence fails
     */
    public String product(HttpServletRequest request, HttpServletResponse response) throws DaoException, IOException {
        Integer id = pathParamId(request);
        Optional<Product> product = id == null ? Optional.empty() : productService.getById(id);
        if (product.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        request.setAttribute("product", product.get());
        UserResponse user = SessionUtil.getUser(request);
        request.setAttribute("cartCount", cartService.getCount(user.getId()));
        return "product.jsp";
    }

    private Integer pathParamId(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) request.getAttribute(Router.PATH_PARAMS_KEY);
        if (params == null) {
            return null;
        }
        String value = params.get(PATH_PARAM_ID);
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
