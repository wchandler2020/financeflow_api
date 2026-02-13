package com.tiltedhat.financeflow_backend.controller;

import com.tiltedhat.financeflow_backend.dto.CreateTransactionRequest;
import com.tiltedhat.financeflow_backend.dto.UpdateTransactionRequest;
import com.tiltedhat.financeflow_backend.dto.CategorySpendingResponse;
import com.tiltedhat.financeflow_backend.dto.MessageResponse;
import com.tiltedhat.financeflow_backend.dto.TransactionResponse;
import com.tiltedhat.financeflow_backend.dto.TransactionSummaryResponse;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * GET /api/transactions
     * Get all transactions for user
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<TransactionResponse> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionService.getTransactionsByDateRange(user, startDate, endDate);
        } else {
            transactions = transactionService.getAllTransactions(user);
        }

        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /api/transactions/{id}
     * Get single transaction
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        TransactionResponse transaction = transactionService.getTransactionById(user, id);
        return ResponseEntity.ok(transaction);
    }

    /**
     * POST /api/transactions
     * Create new transaction
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionResponse transaction = transactionService.createTransaction(user, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transaction);
    }

    /**
     * PUT /api/transactions/{id}
     * Update transaction
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        TransactionResponse transaction = transactionService.updateTransaction(user, id, request);
        return ResponseEntity.ok(transaction);
    }

    /**
     * DELETE /api/transactions/{id}
     * Delete transaction
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        transactionService.deleteTransaction(user, id);
        return ResponseEntity.ok(new MessageResponse("Transaction deleted successfully"));
    }

    /**
     * GET /api/transactions/summary
     * Get income/expense summary
     */
    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(
            @AuthenticationPrincipal User user
    ) {
        TransactionSummaryResponse summary = transactionService.getTransactionSummary(user);
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/transactions/by-category
     * Get spending grouped by category
     */
    @GetMapping("/by-category")
    public ResponseEntity<List<CategorySpendingResponse>> getSpendingByCategory(
            @AuthenticationPrincipal User user
    ) {
        List<CategorySpendingResponse> spending = transactionService.getSpendingByCategory(user);
        return ResponseEntity.ok(spending);
    }
}