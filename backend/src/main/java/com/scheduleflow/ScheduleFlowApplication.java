package com.scheduleflow;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ScheduleFlow Timetable Service — Main Entry Point.
 *
 * <p>Phase 5 Microservice Migration:
 * Registered as TIMETABLE-SERVICE with Eureka Service Discovery Registry.
 * Uses OpenFeign to communicate dynamically with RESOURCE-SERVICE.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.scheduleflow.client")
public class ScheduleFlowApplication {

    public static void main(String[] args) {
        // Automatically load local .env file if present (silently ignored in production/Render)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(ScheduleFlowApplication.class, args);
    }
}
