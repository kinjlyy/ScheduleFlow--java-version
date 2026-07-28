package com.scheduleflow.resource;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ScheduleFlow Resource Service — Main Entry Point.
 *
 * <p>Phase 4 Microservice Migration:
 * Standalone microservice managing physical resources (rooms, labs, auditoriums).
 * Registers with Eureka Service Discovery Registry as {@code RESOURCE-SERVICE}.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ResourceServiceApplication {

    public static void main(String[] args) {
        // Automatically load local .env file if present (silently ignored in production/Render)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(ResourceServiceApplication.class, args);
    }
}
