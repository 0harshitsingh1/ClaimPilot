package com.expenseflow.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseClaimRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Builder.Default
    private String currency = "INR";

    @NotEmpty(message = "At least one expense item is required per claim")
    @Valid
    private List<ExpenseItemRequest> items;
}
