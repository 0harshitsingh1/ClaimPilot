package com.expenseflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
}
