package com.example.navisewebsite;

import com.example.navisewebsite.repository.DatabaseUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NavisewebsiteApplication {

	public static void main(String[] args) {
		// Initialize the database schema when the application starts
		DatabaseUtil.initializeDatabase();
		
		SpringApplication.run(NavisewebsiteApplication.class, args);
	}

}
