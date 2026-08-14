package com.expenseflow.backend.service;

import com.expenseflow.backend.dto.ExpenseClaimRequest;
import com.expenseflow.backend.dto.ExpenseItemRequest;
import com.expenseflow.backend.entity.*;
import com.expenseflow.backend.repository.ExpenseCategoryRepository;
import com.expenseflow.backend.repository.ExpenseClaimRepository;
import com.expenseflow.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExpenseClaimOptimisticLockingTest {

    @Autowired
    private ExpenseClaimRepository claimRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private ExpenseClaimService claimService;

    private User employee;
    private ExpenseCategory category;

    @BeforeEach
    void setUp() {
        employee = userRepository.findByEmail("employee@expenseflow.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("employee@expenseflow.com")
                        .fullName("Employee User")
                        .password("hashed_password")
                        .role(Role.EMPLOYEE)
                        .employeeCode("EMP002")
                        .enabled(true)
                        .build()));

        category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(ExpenseCategory.builder()
                        .name("Test Category")
                        .description("Test")
                        .isActive(true)
                        .build())
        );
    }

    @Test
    void testOptimisticLockingConflictThrows409Exception() {
        // 1. Create a claim
        ExpenseItemRequest itemReq = ExpenseItemRequest.builder()
                .categoryId(category.getId())
                .amount(new BigDecimal("100.00"))
                .expenseDate(LocalDate.now())
                .paymentMethod(PaymentMethod.CARD)
                .build();

        ExpenseClaimRequest createReq = ExpenseClaimRequest.builder()
                .title("Initial Title")
                .items(List.of(itemReq))
                .build();

        var createdResponse = claimService.createDraft(employee, createReq);
        Long claimId = createdResponse.getId();

        // 2. Fetch claim twice in two separate entity instances representing concurrent transactions
        ExpenseClaim claimTx1 = claimRepository.findById(claimId).orElseThrow();
        ExpenseClaim claimTx2 = claimRepository.findById(claimId).orElseThrow();

        // 3. First transaction updates and saves (incrementing DB version from 0 to 1)
        claimTx1.setTitle("Updated by Tx1");
        claimRepository.saveAndFlush(claimTx1);

        // 4. Second transaction tries to save stale entity instance (version still 0 in memory)
        claimTx2.setTitle("Updated by Tx2");
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            claimRepository.saveAndFlush(claimTx2);
        });
    }
}
