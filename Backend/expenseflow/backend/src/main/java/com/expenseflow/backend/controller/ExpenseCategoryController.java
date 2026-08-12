package com.expenseflow.backend.controller;

import com.expenseflow.backend.dto.ExpenseCategoryRequest;
import com.expenseflow.backend.dto.ExpenseCategoryResponse;
import com.expenseflow.backend.service.ExpenseCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExpenseCategoryController {

    private final ExpenseCategoryService categoryService;

    @GetMapping("/api/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExpenseCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.listCategories(false));
    }

    @GetMapping("/api/categories")
    public ResponseEntity<List<ExpenseCategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.listCategories(true));
    }

    @PostMapping("/api/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExpenseCategoryResponse> createCategory(@Valid @RequestBody ExpenseCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @PutMapping("/api/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExpenseCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseCategoryRequest request
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/api/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExpenseCategoryResponse> deactivateCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.deactivateCategory(id));
    }
}
