package com.scheduleflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping({"/", "/api/health"})
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
