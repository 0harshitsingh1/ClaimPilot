package com.expenseflow.backend.service;

import com.expenseflow.backend.dto.*;
import com.expenseflow.backend.entity.*;
import com.expenseflow.backend.exception.ResourceNotFoundException;
import com.expenseflow.backend.repository.ExpenseCategoryRepository;
import com.expenseflow.backend.repository.ExpenseClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseClaimService {

    private final ExpenseClaimRepository claimRepository;
    private final ExpenseCategoryRepository categoryRepository;

    @Transactional
    public ExpenseClaimResponse createDraft(User employee, ExpenseClaimRequest request) {
        Long seq = claimRepository.getNextSequenceValue();
        String claimNumber = String.format("EXP-%d-%06d", LocalDate.now().getYear(), seq);

        ExpenseClaim claim = ExpenseClaim.builder()
                .claimNumber(claimNumber)
                .employee(employee)
                .title(request.getTitle())
                .description(request.getDescription())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(ExpenseStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ExpenseItemRequest itemReq : request.getItems()) {
            ExpenseCategory category = categoryRepository.findById(itemReq.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Expense category not found with id: " + itemReq.getCategoryId()));

            ExpenseItem item = ExpenseItem.builder()
                    .category(category)
                    .amount(itemReq.getAmount())
                    .expenseDate(itemReq.getExpenseDate())
                    .paymentMethod(itemReq.getPaymentMethod())
                    .merchantName(itemReq.getMerchantName())
                    .description(itemReq.getDescription())
                    .projectOrClientName(itemReq.getProjectOrClientName())
                    .build();

            claim.addItem(item);
            totalAmount = totalAmount.add(itemReq.getAmount());
        }

        claim.setTotalAmount(totalAmount);
        ExpenseClaim savedClaim = claimRepository.save(claim);
        return mapToResponse(savedClaim);
    }

    @Transactional
    public ExpenseClaimResponse updateDraft(Long claimId, User user, ExpenseClaimRequest request) {
        ExpenseClaim claim = findClaimAndVerifyOwnership(claimId, user);

        if (claim.getStatus() != ExpenseStatus.DRAFT && claim.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new IllegalArgumentException("Claim cannot be updated in status: " + claim.getStatus());
        }

        // If editing a SUBMITTED claim, reset status back to DRAFT per business rules
        if (claim.getStatus() == ExpenseStatus.SUBMITTED) {
            claim.setStatus(ExpenseStatus.DRAFT);
            claim.setSubmittedAt(null);
        }

        claim.setTitle(request.getTitle());
        claim.setDescription(request.getDescription());
        if (request.getCurrency() != null) {
            claim.setCurrency(request.getCurrency());
        }

        claim.clearItems();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ExpenseItemRequest itemReq : request.getItems()) {
            ExpenseCategory category = categoryRepository.findById(itemReq.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Expense category not found with id: " + itemReq.getCategoryId()));

            ExpenseItem item = ExpenseItem.builder()
                    .category(category)
                    .amount(itemReq.getAmount())
                    .expenseDate(itemReq.getExpenseDate())
                    .paymentMethod(itemReq.getPaymentMethod())
                    .merchantName(itemReq.getMerchantName())
                    .description(itemReq.getDescription())
                    .projectOrClientName(itemReq.getProjectOrClientName())
                    .build();

            claim.addItem(item);
            totalAmount = totalAmount.add(itemReq.getAmount());
        }

        claim.setTotalAmount(totalAmount);
        ExpenseClaim updatedClaim = claimRepository.save(claim);
        return mapToResponse(updatedClaim);
    }

    @Transactional
    public ExpenseClaimResponse submit(Long claimId, User user) {
        ExpenseClaim claim = findClaimAndVerifyOwnership(claimId, user);

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT claims can be submitted");
        }

        claim.setStatus(ExpenseStatus.SUBMITTED);
        claim.setSubmittedAt(LocalDateTime.now());

        ExpenseClaim savedClaim = claimRepository.save(claim);
        return mapToResponse(savedClaim);
    }

    @Transactional
    public ExpenseClaimResponse withdraw(Long claimId, User user) {
        ExpenseClaim claim = findClaimAndVerifyOwnership(claimId, user);

        if (claim.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new IllegalArgumentException("Only SUBMITTED claims can be withdrawn");
        }

        claim.setStatus(ExpenseStatus.DRAFT);

        ExpenseClaim savedClaim = claimRepository.save(claim);
        return mapToResponse(savedClaim);
    }

    @Transactional
    public void deleteDraft(Long claimId, User user) {
        ExpenseClaim claim = findClaimAndVerifyOwnership(claimId, user);

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT claims can be deleted");
        }

        claimRepository.delete(claim);
    }

    @Transactional(readOnly = true)
    public ExpenseClaimResponse getClaimById(Long claimId, User user) {
        ExpenseClaim claim = findClaimAndVerifyOwnership(claimId, user);
        return mapToResponse(claim);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseClaimResponse> listMyClaims(Long employeeId, ExpenseStatus status, Pageable pageable) {
        Page<ExpenseClaim> claims = (status != null)
                ? claimRepository.findByEmployeeIdAndStatus(employeeId, status, pageable)
                : claimRepository.findByEmployeeId(employeeId, pageable);

        return claims.map(this::mapToResponse);
    }

    private ExpenseClaim findClaimAndVerifyOwnership(Long claimId, User user) {
        ExpenseClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense claim not found with id: " + claimId));

        boolean isOwner = claim.getEmployee().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            // Return 404 Not Found for non-owner/non-admin to avoid revealing resource existence
            throw new ResourceNotFoundException("Expense claim not found with id: " + claimId);
        }

        return claim;
    }

    public ExpenseClaimResponse mapToResponse(ExpenseClaim claim) {
        List<ExpenseItemResponse> itemResponses = claim.getItems().stream()
                .map(item -> ExpenseItemResponse.builder()
                        .id(item.getId())
                        .categoryId(item.getCategory().getId())
                        .categoryName(item.getCategory().getName())
                        .amount(item.getAmount())
                        .expenseDate(item.getExpenseDate())
                        .paymentMethod(item.getPaymentMethod())
                        .merchantName(item.getMerchantName())
                        .description(item.getDescription())
                        .projectOrClientName(item.getProjectOrClientName())
                        .build())
                .collect(Collectors.toList());

        return ExpenseClaimResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .employeeId(claim.getEmployee().getId())
                .employeeName(claim.getEmployee().getFullName())
                .title(claim.getTitle())
                .description(claim.getDescription())
                .totalAmount(claim.getTotalAmount())
                .currency(claim.getCurrency())
                .status(claim.getStatus())
                .submittedAt(claim.getSubmittedAt())
                .items(itemResponses)
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
