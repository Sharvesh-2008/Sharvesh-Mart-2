package com.sharvesh.sharvesh_mart.controller;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sharvesh.sharvesh_mart.dao.DaoFactory;
import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.exception.DuplicateEmailException;
import com.sharvesh.sharvesh_mart.exception.InsufficientStockException;
import com.sharvesh.sharvesh_mart.exception.ValidationException;
import com.sharvesh.sharvesh_mart.listener.AppContextListener;
import com.sharvesh.sharvesh_mart.service.CartService;
import com.sharvesh.sharvesh_mart.service.OrderService;
import com.sharvesh.sharvesh_mart.service.ProductService;
import com.sharvesh.sharvesh_mart.service.UserService;

/**
 * Front Controller servlet (spec Section 2). Resolves each request to a thin
 * controller action, forwards static assets to the container default servlet,
 * and centralizes error mapping. No SQL or business logic lives here.
 */
public class DispatcherServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServlet.class);

    private static final String VIEWS_PREFIX = "/WEB-INF/views/";
    private static final String REDIRECT_PREFIX = "redirect:";

    private Router router;

    @Override
    public void init() {
        DataSource dataSource =
                (DataSource) getServletContext().getAttribute(AppContextListener.DATA_SOURCE_KEY);
        if (dataSource == null) {
            throw new IllegalStateException("Data source not initialized by the ServletContextListener.");
        }
        DaoFactory daoFactory = new DaoFactory(dataSource);
        UserService userService = new UserService(daoFactory.getUserDao());
        ProductService productService = new ProductService(daoFactory.getProductDao());
        CartService cartService = new CartService(daoFactory.getCartDao(), daoFactory.getProductDao());
        OrderService orderService = new OrderService(daoFactory.getCartDao(), daoFactory.getOrderDao());

        AuthController authController = new AuthController(userService);
        HealthController healthController = new HealthController();
        CatalogController catalogController = new CatalogController(productService, cartService);
        CartController cartController = new CartController(cartService);
        OrderController orderController = new OrderController(orderService, cartService);

        router = new Router();
        router.register("GET", "/", authController::index);
        router.register("GET", "/login", authController::showLogin);
        router.register("POST", "/login", authController::login);
        router.register("GET", "/register", authController::showRegister);
        router.register("POST", "/register", authController::register);
        router.register("GET", "/logout", authController::logout);
        router.register("GET", "/home", authController::home);
        router.register("GET", "/api/v1/health", healthController::handle);

        router.register("GET", "/products", catalogController::catalog);
        router.register("GET", "/products/{id}", catalogController::product);
        router.register("GET", "/cart", cartController::cart);
        router.register("POST", "/cart/add", cartController::add);
        router.register("POST", "/cart/update", cartController::update);
        router.register("POST", "/cart/remove", cartController::remove);
        router.register("GET", "/checkout", orderController::checkout);
        router.register("POST", "/checkout", orderController::place);
        router.register("GET", "/orders", orderController::orders);
        router.register("GET", "/orders/{id}", orderController::order);
        router.register("GET", "/seller/orders", orderController::sellerOrders);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.startsWith("/static/")) {
            getServletContext().getNamedDispatcher("default").forward(request, response);
            return;
        }

        String method = request.getMethod().toUpperCase(Locale.ROOT);
        Optional<Router.Match> match = router.resolve(method, path);
        if (match.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!match.get().pathParams().isEmpty()) {
            request.setAttribute(Router.PATH_PARAMS_KEY, match.get().pathParams());
        }

        try {
            String result = match.get().controller().handle(request, response);
            dispatch(result, request, response);
        } catch (ValidationException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid input: " + e.getMessage());
        } catch (DuplicateEmailException e) {
            sendError(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (InsufficientStockException e) {
            sendError(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (DaoException e) {
            LOGGER.error("Persistence error while processing {} {}", method, path, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOGGER.error("Unhandled error while processing {} {}", method, path, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void dispatch(String result, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (result == null) {
            return;
        }
        if (result.startsWith(REDIRECT_PREFIX)) {
            response.sendRedirect(result.substring(REDIRECT_PREFIX.length()));
            return;
        }
        request.getRequestDispatcher(VIEWS_PREFIX + result).forward(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        LOGGER.warn("Request failed with status {}: {}", status, message);
        response.sendError(status, message);
    }
}
