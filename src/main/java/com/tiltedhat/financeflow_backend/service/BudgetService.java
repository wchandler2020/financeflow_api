package com.tiltedhat.financeflow_backend.service;


import com.tiltedhat.financeflow_backend.dto.CreateBudgetRequest;
import com.tiltedhat.financeflow_backend.dto.UpdateBudgetRequest;
import com.tiltedhat.financeflow_backend.dto.BudgetResponse;
import com.tiltedhat.financeflow_backend.entity.Budget;
import com.tiltedhat.financeflow_backend.entity.Category;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.dto.BudgetMapper;
import com.tiltedhat.financeflow_backend.repository.BudgetRepository;
import com.tiltedhat.financeflow_backend.repository.CategoryRepository;
import com.tiltedhat.financeflow_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetMapper budgetMapper;

    /**
     * Get all budgets for user
     */
    public List<BudgetResponse> getAllBudgets(User user) {
        return budgetRepository.findByUser(user)
                .stream()
                .map(budget -> {
                    BigDecimal spent = transactionRepository.calculateSpendingForCategoryInMonth(
                            user,
                            budget.getCategory().getId(),
                            budget.getMonth(),
                            budget.getYear()
                    );
                    return budgetMapper.toResponse(budget, spent);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get budgets for specific month/year
     */
    public List<BudgetResponse> getBudgetsByMonth(User user, Integer month, Integer year) {
        return budgetRepository.findByUserAndMonthAndYear(user, month, year)
                .stream()
                .map(budget -> {
                    BigDecimal spent = transactionRepository.calculateSpendingForCategoryInMonth(
                            user,
                            budget.getCategory().getId(),
                            month,
                            year
                    );
                    return budgetMapper.toResponse(budget, spent);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get current month's budgets
     */
    public List<BudgetResponse> getCurrentMonthBudgets(User user) {
        LocalDate now = LocalDate.now();
        return getBudgetsByMonth(user, now.getMonthValue(), now.getYear());
    }

    /**
     * Get single budget by ID
     */
    public BudgetResponse getBudgetById(User user, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new RuntimeException("Budget not found or access denied"));

        BigDecimal spent = transactionRepository.calculateSpendingForCategoryInMonth(
                user,
                budget.getCategory().getId(),
                budget.getMonth(),
                budget.getYear()
        );

        return budgetMapper.toResponse(budget, spent);
    }

    /**
     * Create new budget
     */
    @Transactional
    public BudgetResponse createBudget(User user, CreateBudgetRequest request) {
        // Verify category exists and is accessible to user
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getIsSystem() && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Category not found or access denied");
        }

        // Check if budget already exists for this category/month/year
        if (budgetRepository.existsByUserAndCategoryIdAndMonthAndYear(
                user, request.getCategoryId(), request.getMonth(), request.getYear())) {
            throw new RuntimeException("Budget already exists for this category in the specified month");
        }

        // Create budget
        Budget budget = budgetMapper.toEntity(request);
        budget.setUser(user);
        budget.setCategory(category);

        Budget saved = budgetRepository.save(budget);

        // Get current spending
        BigDecimal spent = transactionRepository.calculateSpendingForCategoryInMonth(
                user,
                category.getId(),
                request.getMonth(),
                request.getYear()
        );

        return budgetMapper.toResponse(saved, spent);
    }

    /**
     * Update budget
     */
    @Transactional
    public BudgetResponse updateBudget(User user, Long budgetId, UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new RuntimeException("Budget not found or access denied"));

        budget.setAmount(request.getAmount());

        Budget updated = budgetRepository.save(budget);

        BigDecimal spent = transactionRepository.calculateSpendingForCategoryInMonth(
                user,
                budget.getCategory().getId(),
                budget.getMonth(),
                budget.getYear()
        );

        return budgetMapper.toResponse(updated, spent);
    }

    /**
     * Delete budget
     */
    @Transactional
    public void deleteBudget(User user, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new RuntimeException("Budget not found or access denied"));

        budgetRepository.delete(budget);
    }
}
