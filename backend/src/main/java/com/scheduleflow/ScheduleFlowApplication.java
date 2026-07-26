package com.scheduleflow;

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
        SpringApplication.run(ScheduleFlowApplication.class, args);
    }
}
