package com.sharvesh.sharvesh_mart.controller;

/**
 * Immutable binding between an HTTP method + path pair and its controller
 * action, used by the {@link Router}.
 *
 * @param method     the HTTP method (GET, POST, ...)
 * @param path       the application-relative path (e.g. {@code /login})
 * @param controller the action to execute
 */
public record Route(String method, String path, Controller controller) {
}
