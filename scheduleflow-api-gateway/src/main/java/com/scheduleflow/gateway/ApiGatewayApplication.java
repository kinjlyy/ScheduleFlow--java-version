package com.scheduleflow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ScheduleFlow API Gateway — Main Entry Point.
 *
 * <p>Phase 3 Microservice Migration:
 * Serves as the standalone entry point for client requests.
 * Registers with Eureka as {@code API-GATEWAY} and dynamically routes requests starting with
 * {@code /api/**} to {@code TIMETABLE-SERVICE} using Spring Cloud LoadBalancer ({@code lb://TIMETABLE-SERVICE}).
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
