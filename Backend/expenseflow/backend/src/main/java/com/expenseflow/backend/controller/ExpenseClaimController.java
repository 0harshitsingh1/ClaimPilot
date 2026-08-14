package com.expenseflow.backend.controller;

import com.expenseflow.backend.dto.ExpenseClaimRequest;
import com.expenseflow.backend.dto.ExpenseClaimResponse;
import com.expenseflow.backend.entity.ExpenseStatus;
import com.expenseflow.backend.entity.User;
import com.expenseflow.backend.exception.ResourceNotFoundException;
import com.expenseflow.backend.repository.UserRepository;
import com.expenseflow.backend.service.ExpenseClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseClaimController {

    private final ExpenseClaimService claimService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ExpenseClaimResponse> createDraft(@Valid @RequestBody ExpenseClaimRequest request) {
        User currentUser = getCurrentUser();
        ExpenseClaimResponse response = claimService.createDraft(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseClaimResponse>> listMyClaims(
            @RequestParam(required = false) ExpenseStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User currentUser = getCurrentUser();
        Page<ExpenseClaimResponse> claims = claimService.listMyClaims(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseClaimResponse> getClaimById(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(claimService.getClaimById(id, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseClaimResponse> updateDraft(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseClaimRequest request
    ) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(claimService.updateDraft(id, currentUser, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDraft(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        claimService.deleteDraft(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ExpenseClaimResponse> submitClaim(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(claimService.submit(id, currentUser));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ExpenseClaimResponse> withdrawClaim(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(claimService.withdraw(id, currentUser));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found with email: " + email));
    }
}
