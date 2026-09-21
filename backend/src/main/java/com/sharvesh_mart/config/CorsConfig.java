package com.sharvesh_mart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        registry.addMapping("/api/v1/**")
                .allowedOrigins(origins)
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
