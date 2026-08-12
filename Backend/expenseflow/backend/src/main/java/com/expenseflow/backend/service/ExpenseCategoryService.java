package com.expenseflow.backend.service;

import com.expenseflow.backend.dto.ExpenseCategoryRequest;
import com.expenseflow.backend.dto.ExpenseCategoryResponse;
import com.expenseflow.backend.entity.ExpenseCategory;
import com.expenseflow.backend.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository categoryRepository;

    @Transactional
    public ExpenseCategoryResponse createCategory(ExpenseCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Expense category with name '" + request.getName() + "' already exists");
        }

        ExpenseCategory category = ExpenseCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();

        ExpenseCategory savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    @Transactional
    public ExpenseCategoryResponse updateCategory(Long id, ExpenseCategoryRequest request) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense category not found with id: " + id));

        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Expense category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        ExpenseCategory updatedCategory = categoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    @Transactional
    public ExpenseCategoryResponse deactivateCategory(Long id) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense category not found with id: " + id));

        category.setActive(false);
        ExpenseCategory savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategoryResponse> listCategories(boolean activeOnly) {
        List<ExpenseCategory> categories = activeOnly
                ? categoryRepository.findByIsActiveTrue()
                : categoryRepository.findAll();

        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ExpenseCategoryResponse mapToResponse(ExpenseCategory category) {
        return ExpenseCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
