package com.expenseflow.backend.service;

import com.expenseflow.backend.dto.*;
import com.expenseflow.backend.entity.*;
import com.expenseflow.backend.exception.ResourceNotFoundException;
import com.expenseflow.backend.repository.ExpenseCategoryRepository;
import com.expenseflow.backend.repository.ExpenseClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseClaimServiceTest {

    @Mock
    private ExpenseClaimRepository claimRepository;

    @Mock
    private ExpenseCategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseClaimService claimService;

    private User employee;
    private User otherEmployee;
    private User admin;
    private ExpenseCategory category1;
    private ExpenseCategory category2;

    @BeforeEach
    void setUp() {
        employee = User.builder().id(1L).email("employee@expenseflow.com").fullName("Employee One").role(Role.EMPLOYEE).build();
        otherEmployee = User.builder().id(2L).email("other@expenseflow.com").fullName("Employee Two").role(Role.EMPLOYEE).build();
        admin = User.builder().id(3L).email("admin@expenseflow.com").fullName("Admin User").role(Role.ADMIN).build();

        category1 = ExpenseCategory.builder().id(1L).name("Travel").isActive(true).build();
        category2 = ExpenseCategory.builder().id(2L).name("Food and meals").isActive(true).build();
    }

    @Test
    void testCreateDraftSuccess() {
        ExpenseItemRequest item1 = ExpenseItemRequest.builder()
                .categoryId(1L)
                .amount(new BigDecimal("150.00"))
                .expenseDate(LocalDate.now())
                .paymentMethod(PaymentMethod.CARD)
                .merchantName("Airline")
                .build();

        ExpenseItemRequest item2 = ExpenseItemRequest.builder()
                .categoryId(2L)
                .amount(new BigDecimal("50.00"))
                .expenseDate(LocalDate.now())
                .paymentMethod(PaymentMethod.CASH)
                .merchantName("Restaurant")
                .build();

        ExpenseClaimRequest req = ExpenseClaimRequest.builder()
                .title("Trip to NY")
                .description("Client visit")
                .currency("INR")
                .items(List.of(item1, item2))
                .build();

        when(claimRepository.getNextSequenceValue()).thenReturn(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category2));
        when(claimRepository.save(any(ExpenseClaim.class))).thenAnswer(invocation -> {
            ExpenseClaim c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        ExpenseClaimResponse res = claimService.createDraft(employee, req);

        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals("EXP-" + LocalDate.now().getYear() + "-000001", res.getClaimNumber());
        assertEquals(new BigDecimal("200.00"), res.getTotalAmount());
        assertEquals(ExpenseStatus.DRAFT, res.getStatus());
        assertEquals(2, res.getItems().size());
    }

    @Test
    void testUpdateSubmittedClaimResetsToDraft() {
        ExpenseClaim existing = ExpenseClaim.builder()
                .id(10L)
                .claimNumber("EXP-2026-000001")
                .employee(employee)
                .title("Old Title")
                .status(ExpenseStatus.SUBMITTED)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        ExpenseItemRequest item1 = ExpenseItemRequest.builder()
                .categoryId(1L)
                .amount(new BigDecimal("250.00"))
                .expenseDate(LocalDate.now())
                .paymentMethod(PaymentMethod.UPI)
                .build();

        ExpenseClaimRequest req = ExpenseClaimRequest.builder()
                .title("Updated Title")
                .items(List.of(item1))
                .build();

        when(claimRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(claimRepository.save(any(ExpenseClaim.class))).thenAnswer(i -> i.getArgument(0));

        ExpenseClaimResponse res = claimService.updateDraft(10L, employee, req);

        assertEquals("Updated Title", res.getTitle());
        assertEquals(ExpenseStatus.DRAFT, res.getStatus());
        assertNull(res.getSubmittedAt());
        assertEquals(new BigDecimal("250.00"), res.getTotalAmount());
    }

    @Test
    void testGetClaimBelongingToOtherEmployeeReturns404() {
        ExpenseClaim existing = ExpenseClaim.builder()
                .id(10L)
                .claimNumber("EXP-2026-000001")
                .employee(otherEmployee)
                .title("Other Claim")
                .status(ExpenseStatus.DRAFT)
                .build();

        when(claimRepository.findById(10L)).thenReturn(Optional.of(existing));

        assertThrows(ResourceNotFoundException.class, () -> claimService.getClaimById(10L, employee));
    }

    @Test
    void testGetClaimBelongingToOtherEmployeeAllowedForAdmin() {
        ExpenseClaim existing = ExpenseClaim.builder()
                .id(10L)
                .claimNumber("EXP-2026-000001")
                .employee(otherEmployee)
                .title("Other Claim")
                .status(ExpenseStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        when(claimRepository.findById(10L)).thenReturn(Optional.of(existing));

        ExpenseClaimResponse res = claimService.getClaimById(10L, admin);
        assertNotNull(res);
        assertEquals(10L, res.getId());
    }
}
