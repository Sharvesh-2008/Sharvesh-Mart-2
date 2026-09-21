package com.sharvesh.sharvesh_mart.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sharvesh.sharvesh_mart.listener.AppContextListener;
import com.sharvesh.sharvesh_mart.util.JsonUtil;

/**
 * Liveness/readiness endpoint: {@code GET /api/v1/health} returns
 * {@code {"status":"UP","db":"UP"}} after verifying database connectivity
 * (spec Section 18 rule 1).
 */
public class HealthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);

    /**
     * Checks database connectivity and returns the health payload.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return {@code null} (the response is written directly)
     * @throws SQLException if the status check itself fails unexpectedly
     */
    public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        DataSource dataSource =
                (DataSource) request.getServletContext().getAttribute(AppContextListener.DATA_SOURCE_KEY);
        String dbStatus = "UP";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                dbStatus = "DOWN";
            }
        } catch (SQLException e) {
            LOGGER.error("Database health check failed.", e);
            dbStatus = "DOWN";
        }

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("status", dbStatus);
        payload.put("db", dbStatus);

        try {
            JsonUtil.write(response, HttpServletResponse.SC_OK, payload);
        } catch (Exception e) {
            LOGGER.error("Failed to write health response.", e);
        }
        return null;
    }
}
