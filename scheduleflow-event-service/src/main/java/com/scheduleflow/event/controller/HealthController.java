package com.scheduleflow.event.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check endpoints for EVENT-SERVICE.
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<String> rootHealth() {
        return ResponseEntity.ok("ScheduleFlow Event Service is running");
    }

    @GetMapping("/api/events/health")
    public ResponseEntity<String> apiHealth() {
        return ResponseEntity.ok("ScheduleFlow Event Service API is running");
    }
}
