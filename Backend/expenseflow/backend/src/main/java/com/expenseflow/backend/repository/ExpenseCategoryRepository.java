package com.expenseflow.backend.repository;

import com.expenseflow.backend.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    List<ExpenseCategory> findByIsActiveTrue();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<ExpenseCategory> findByName(String name);
}
