package com.saranya.saranyamart.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central route registry for the Front Controller. Maps HTTP method + path
 * pairs to controller actions. Paths may contain single-segment placeholders
 * written as {@code {name}} (e.g. {@code /products/{id}}); matched values are
 * returned with the resolved action.
 */
public class Router {

    /** Request attribute key holding the map of captured path parameters. */
    public static final String PATH_PARAMS_KEY = "router.pathParams";

    private final List<Route> routes = new ArrayList<>();

    /**
     * Registers an action for a method and path.
     *
     * @param method     the HTTP method
     * @param path       the application-relative path
     * @param controller the action
     */
    public void register(String method, String path, Controller controller) {
        routes.add(new Route(method, path, controller));
    }

    /**
     * Resolves the action bound to a method and path.
     *
     * @param method the HTTP method
     * @param path   the application-relative path
     * @return the matching action and any path parameters, or
     *         {@link Optional#empty()} if none matches
     */
    public Optional<Match> resolve(String method, String path) {
        for (Route route : routes) {
            if (!route.method().equals(method)) {
                continue;
            }
            Map<String, String> pathParams = matchSegments(route.path(), path);
            if (pathParams != null) {
                return Optional.of(new Match(route.controller(), pathParams));
            }
        }
        return Optional.empty();
    }

    private Map<String, String> matchSegments(String pattern, String path) {
        String[] patternSegments = pattern.split("/");
        String[] pathSegments = path.split("/");
        if (patternSegments.length != pathSegments.length) {
            return null;
        }
        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < patternSegments.length; i++) {
            String patternSegment = patternSegments[i];
            String pathSegment = pathSegments[i];
            if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
                params.put(patternSegment.substring(1, patternSegment.length() - 1), pathSegment);
            } else if (!patternSegment.equals(pathSegment)) {
                return null;
            }
        }
        return params;
    }

    /**
     * A resolved action together with any path parameters extracted from the URL.
     *
     * @param controller the action to execute
     * @param pathParams the captured path parameters, possibly empty
     */
    public record Match(Controller controller, Map<String, String> pathParams) {
    }
}
