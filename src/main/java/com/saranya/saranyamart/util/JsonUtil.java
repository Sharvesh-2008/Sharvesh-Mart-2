package com.saranya.saranyamart.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.saranya.saranyamart.dto.ApiResponse;

/**
 * JSON serialization helpers backed by Gson for the {@code /api/v1/...}
 * endpoints (spec Section 13).
 */
public final class JsonUtil {

    private static final Gson GSON = new Gson();

    private JsonUtil() {
        // Utility class, not instantiable.
    }

    /**
     * Serializes an object to a JSON string.
     *
     * @param value the object to serialize
     * @return the JSON string
     */
    public static String toJson(Object value) {
        return GSON.toJson(value);
    }

    /**
     * Writes a JSON response with the given HTTP status and content type.
     *
     * @param response the HTTP response to write to
     * @param status   the HTTP status code
     * @param payload  the object to serialize
     * @throws IOException if the response cannot be written
     */
    public static void write(HttpServletResponse response, int status, Object payload) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(GSON.toJson(payload));
    }

    /**
     * Writes a standard success envelope (spec Section 13 rule 2).
     *
     * @param response the HTTP response to write to
     * @param status   the HTTP status code
     * @param data     the payload carried by the envelope
     * @param <T>      the payload type
     * @throws IOException if the response cannot be written
     */
    public static <T> void writeSuccess(HttpServletResponse response, int status, T data) throws IOException {
        write(response, status, ApiResponse.ok(data));
    }
}
