package com.scheduleflow.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ScheduleFlow Eureka Server — Central Service Discovery Registry.
 *
 * <p>Exposes the Eureka Dashboard at {@code http://localhost:8761}.
 * All microservices (TIMETABLE-SERVICE, RESOURCE-SERVICE, etc.) register with
 * this Eureka Server to enable dynamic location lookup, load balancing, and health tracking.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
