package com.expenseflow.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/v1/dummy-protected")
    public ResponseEntity<String> dummyProtected() {
        return ResponseEntity.ok("Success");
    }
}
