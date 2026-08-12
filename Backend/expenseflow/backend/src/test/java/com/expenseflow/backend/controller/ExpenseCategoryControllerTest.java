package com.expenseflow.backend.controller;

import com.expenseflow.backend.dto.ExpenseCategoryRequest;
import com.expenseflow.backend.dto.ExpenseCategoryResponse;
import com.expenseflow.backend.service.ExpenseCategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class ExpenseCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseCategoryService categoryService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllCategoriesAsAdmin() throws Exception {
        ExpenseCategoryResponse cat = ExpenseCategoryResponse.builder()
                .id(1L)
                .name("Travel")
                .description("Travel expenses")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryService.listCategories(false)).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Travel"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetAllCategoriesAsEmployeeForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetActiveCategoriesAsEmployee() throws Exception {
        ExpenseCategoryResponse cat = ExpenseCategoryResponse.builder()
                .id(1L)
                .name("Travel")
                .description("Travel expenses")
                .isActive(true)
                .build();

        when(categoryService.listCategories(true)).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Travel"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategorySuccess() throws Exception {
        ExpenseCategoryRequest req = new ExpenseCategoryRequest("Software", "Software licenses");
        ExpenseCategoryResponse res = ExpenseCategoryResponse.builder()
                .id(2L)
                .name("Software")
                .description("Software licenses")
                .isActive(true)
                .build();

        when(categoryService.createCategory(any())).thenReturn(res);

        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Software"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategoryDuplicateNameError() throws Exception {
        ExpenseCategoryRequest req = new ExpenseCategoryRequest("Travel", "Duplicate category");

        when(categoryService.createCategory(any()))
                .thenThrow(new IllegalArgumentException("Expense category with name 'Travel' already exists"));

        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Expense category with name 'Travel' already exists"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeactivateCategory() throws Exception {
        ExpenseCategoryResponse res = ExpenseCategoryResponse.builder()
                .id(1L)
                .name("Travel")
                .isActive(false)
                .build();

        when(categoryService.deactivateCategory(eq(1L))).thenReturn(res);

        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }
}
