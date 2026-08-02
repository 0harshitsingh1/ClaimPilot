package com.expenseflow.backend.controller;

import com.expenseflow.backend.dto.HealthResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<HealthResponseDto> getHealthStatus() {
        HealthResponseDto healthResponse = HealthResponseDto.builder()
                .status("UP")
                .service("backend")
                .timestamp(Instant.now().toString())
                .build();
        return ResponseEntity.ok(healthResponse);
    }
}
