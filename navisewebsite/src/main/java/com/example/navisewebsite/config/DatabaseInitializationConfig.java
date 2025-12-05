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
            // Initialize databases and create tables on application startup
            DatabaseUtil.initializeDatabases();
            System.out.println("Databases initialized successfully.");
        };
    }
}
