package com.example.navisewebsite.config;

import com.example.navisewebsite.repository.DatabaseUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInitializationConfig {

    @Bean
    public CommandLineRunner initializeDatabases() {
        return args -> {
            System.out.println("========================================");
            System.out.println("Starting Database Initialization...");
            System.out.println("========================================");
            // Initialize databases and create tables on application startup
            DatabaseUtil.initializeDatabases();
            System.out.println("========================================");
            System.out.println("✓ Databases initialized successfully.");
            System.out.println("========================================");
        };
    }
}
