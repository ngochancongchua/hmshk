package com.hmshk;
import java.util.List;

import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@SpringBootApplication
public class HotelManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelManagementApplication.class, args);
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*") // Allow all origins
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow specific methods
                        .allowedHeaders("*"); // Allow all headers
            }
        };
    }
    
    @Bean
    CommandLineRunner checkDatabaseContent(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                System.out.println("\n--- CHECKING DATABASE CONTENT ---");
                
                // Check if staff table exists
                List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()");
                System.out.println("Tables in database: " + tables);
                
                // Try to query staff table(for debug!!!)
                try {
                    List<Map<String, Object>> staff = jdbcTemplate.queryForList("SELECT * FROM staff");
                    System.out.println("Staff records found: " + staff.size());
                    for (Map<String, Object> record : staff) {
                        System.out.println(record);
                    }
                } catch (Exception e) {
                    System.out.println("Error querying staff table: " + e.getMessage());
                }
                
                System.out.println("--- DATABASE CHECK COMPLETE ---\n");
            } catch (Exception e) {
                System.err.println("Error checking database: " + e.getMessage());
            }
        };
    }
}
