package com.expenseflow.backend.dto;

import com.expenseflow.backend.entity.ExpenseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseClaimResponse {

    private Long id;
    private String claimNumber;
    private Long employeeId;
    private String employeeName;
    private String title;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private ExpenseStatus status;
    private LocalDateTime submittedAt;
    private List<ExpenseItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
