package com.scheduleflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Basic health check endpoints.
 *
 * Spring Boot Actuator also provides /actuator/health for standard health checks.
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<String> rootHealth() {
        return ResponseEntity.ok("ScheduleFlow Timetable Service is running");
    }

    @GetMapping("/api/health")
    public ResponseEntity<String> apiHealth() {
        return ResponseEntity.ok("ScheduleFlow API is running");
    }
}
