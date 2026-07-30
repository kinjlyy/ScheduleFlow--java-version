package com.scheduleflow.resource.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check endpoints for RESOURCE-SERVICE.
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<String> rootHealth() {
        return ResponseEntity.ok("ScheduleFlow Resource Service is running");
    }

    @GetMapping("/api/health")
    public ResponseEntity<String> apiHealth() {
        return ResponseEntity.ok("ScheduleFlow Resource Service API is running");
    }
}
