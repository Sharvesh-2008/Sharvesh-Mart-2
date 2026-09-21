package com.sharvesh.sharvesh_mart.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A single request-handling action resolved by the {@link DispatcherServlet}.
 * Returns a view path (forwarded to the JSP), a {@code redirect:/path} target,
 * or {@code null} when the handler already wrote the response.
 */
public interface Controller {

    /**
     * Handles the request.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the view path, a redirect target, or {@code null}
     * @throws Exception if processing fails (the dispatcher maps it to an error)
     */
    String handle(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
