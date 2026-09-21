package com.sharvesh_mart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sharvesh_mart.db.DatabaseManager;

/**
 * Main Spring Boot Entry Point for SharveshMart E-Commerce Platform.
 * Embedded Tomcat web engine automatically boots on port 8080.
 */
@SpringBootApplication
public class SharveshMartApplication {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("                SHARVESHMART E-COMMERCE PLATFORM                  ");
        System.out.println("              Spring Boot & Embedded Tomcat Engine               ");
        System.out.println("==================================================================");

        // Initialize Database & Seed Default Accounts
        DatabaseManager.initializeDatabase();

        // Start Spring Boot Embedded Tomcat Application
        SpringApplication.run(SharveshMartApplication.class, args);

        System.out.println("\n[SUCCESS] SharveshMart Spring Boot Application is running live!");
        System.out.println("-> Access Website at: http://localhost:8080");
        System.out.println("-> Default Admin Account: admin@SharveshMart.com / Admin@123");
        System.out.println("-> Default Seller Account: seller@SharveshMart.com / Seller@123");
        System.out.println("-> Default Buyer Account: buyer@SharveshMart.com / Buyer@123");
        System.out.println("------------------------------------------------------------------\n");
    }
}

