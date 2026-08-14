package com.expenseflow.backend.repository;

import com.expenseflow.backend.entity.ExpenseClaim;
import com.expenseflow.backend.entity.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Long> {

    Page<ExpenseClaim> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<ExpenseClaim> findByEmployeeIdAndStatus(Long employeeId, ExpenseStatus status, Pageable pageable);

    Page<ExpenseClaim> findByStatus(ExpenseStatus status, Pageable pageable);

    @Query(value = "SELECT nextval('expense_claim_seq')", nativeQuery = true)
    Long getNextSequenceValue();
}
