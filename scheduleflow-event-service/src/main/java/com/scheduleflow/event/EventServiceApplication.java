package com.scheduleflow.event;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ScheduleFlow Event Service — Standalone Microservice Entry Point.
 *
 * <p>Phase 7A Microservice Architecture:
 * Manages room reservations, academic events, and prepares interfaces for timetable impact analysis.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class EventServiceApplication {

    public static void main(String[] args) {
        // Automatically load local .env file if present (silently ignored in production/Render)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(EventServiceApplication.class, args);
    }
}
