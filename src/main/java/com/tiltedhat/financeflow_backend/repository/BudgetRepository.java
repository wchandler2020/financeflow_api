package com.tiltedhat.financeflow_backend.repository;

import com.tiltedhat.financeflow_backend.entity.Budget;
import com.tiltedhat.financeflow_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Find all budgets for a user
    List<Budget> findByUser(User user);

    // Find budget by ID and user (permission check)
    Optional<Budget> findByIdAndUser(Long id, User user);

    // Find budgets for a specific month/year
    List<Budget> findByUserAndMonthAndYear(User user, Integer month, Integer year);

    // Find budget for specific category and month
    Optional<Budget> findByUserAndCategoryIdAndMonthAndYear(
            User user,
            Long categoryId,
            Integer month,
            Integer year
    );

    // Check if budget exists for category in month
    boolean existsByUserAndCategoryIdAndMonthAndYear(
            User user,
            Long categoryId,
            Integer month,
            Integer year
    );

    // Get all budgets for current month
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.month = :month AND b.year = :year")
    List<Budget> findCurrentMonthBudgets(
            @Param("user") User user,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
}
