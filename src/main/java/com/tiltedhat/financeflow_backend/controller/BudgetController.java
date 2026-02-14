package com.tiltedhat.financeflow_backend.controller;

import com.tiltedhat.financeflow_backend.dto.CreateBudgetRequest;
import com.tiltedhat.financeflow_backend.dto.UpdateBudgetRequest;
import com.tiltedhat.financeflow_backend.dto.BudgetResponse;
import com.tiltedhat.financeflow_backend.dto.MessageResponse;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * GET /api/budgets
     * Get all budgets for user
     */
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets(
            @AuthenticationPrincipal User user
    ) {
        List<BudgetResponse> budgets = budgetService.getAllBudgets(user);
        return ResponseEntity.ok(budgets);
    }

    /**
     * GET /api/budgets/current
     * Get current month's budgets
     */
    @GetMapping("/current")
    public ResponseEntity<List<BudgetResponse>> getCurrentMonthBudgets(
            @AuthenticationPrincipal User user
    ) {
        List<BudgetResponse> budgets = budgetService.getCurrentMonthBudgets(user);
        return ResponseEntity.ok(budgets);
    }

    /**
     * GET /api/budgets?month=1&year=2025
     * Get budgets for specific month
     */
    @GetMapping(params = {"month", "year"})
    public ResponseEntity<List<BudgetResponse>> getBudgetsByMonth(
            @AuthenticationPrincipal User user,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        List<BudgetResponse> budgets = budgetService.getBudgetsByMonth(user, month, year);
        return ResponseEntity.ok(budgets);
    }

    /**
     * GET /api/budgets/{id}
     * Get single budget
     */
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        BudgetResponse budget = budgetService.getBudgetById(user, id);
        return ResponseEntity.ok(budget);
    }

    /**
     * POST /api/budgets
     * Create new budget
     */
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        BudgetResponse budget = budgetService.createBudget(user, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(budget);
    }

    /**
     * PUT /api/budgets/{id}
     * Update budget
     */
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBudgetRequest request
    ) {
        BudgetResponse budget = budgetService.updateBudget(user, id, request);
        return ResponseEntity.ok(budget);
    }

    /**
     * DELETE /api/budgets/{id}
     * Delete budget
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteBudget(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        budgetService.deleteBudget(user, id);
        return ResponseEntity.ok(new MessageResponse("Budget deleted successfully"));
    }
}