package com.scheduleflow.resource;

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
        SpringApplication.run(ResourceServiceApplication.class, args);
    }
}
