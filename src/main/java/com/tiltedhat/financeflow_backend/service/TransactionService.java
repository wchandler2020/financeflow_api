package com.tiltedhat.financeflow_backend.service;


import com.tiltedhat.financeflow_backend.dto.CreateTransactionRequest;
import com.tiltedhat.financeflow_backend.dto.UpdateTransactionRequest;
import com.tiltedhat.financeflow_backend.dto.CategorySpendingResponse;
import com.tiltedhat.financeflow_backend.dto.TransactionResponse;
import com.tiltedhat.financeflow_backend.dto.TransactionSummaryResponse;
import com.tiltedhat.financeflow_backend.entity.*;
import com.tiltedhat.financeflow_backend.dto.TransactionMapper;
import com.tiltedhat.financeflow_backend.repository.AccountRepository;
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
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    /**
     * Get all transactions for user
     */
    public List<TransactionResponse> getAllTransactions(User user) {
        return transactionRepository.findByUserOrderByTransactionDateDesc(user)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get transactions by date range
     */
    public List<TransactionResponse> getTransactionsByDateRange(
            User user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return transactionRepository.findByUserAndDateRange(user, startDate, endDate)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get single transaction by ID
     */
    public TransactionResponse getTransactionById(User user, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));

        return transactionMapper.toResponse(transaction);
    }

    /**
     * Create new transaction
     */
    @Transactional
    public TransactionResponse createTransaction(User user, CreateTransactionRequest request) {
        // Verify account belongs to user
        Account account = accountRepository.findByIdAndUser(request.getAccountId(), user)
                .orElseThrow(() -> new RuntimeException("Account not found or access denied"));

        // Verify category is accessible to user (system or user's custom)
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getIsSystem() && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Category not found or access denied");
        }

        // Create transaction
        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setCategory(category);

        // Update account balance
        updateAccountBalance(account, request.getAmount(), request.getType(), true);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    /**
     * Update existing transaction
     */
    @Transactional
    public TransactionResponse updateTransaction(
            User user,
            Long transactionId,
            UpdateTransactionRequest request
    ) {
        // Find transaction
        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));

        // Verify new account belongs to user
        Account newAccount = accountRepository.findByIdAndUser(request.getAccountId(), user)
                .orElseThrow(() -> new RuntimeException("Account not found or access denied"));

        // Verify new category
        Category newCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!newCategory.getIsSystem() && !newCategory.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Category not found or access denied");
        }

        // Reverse old transaction from old account
        updateAccountBalance(
                transaction.getAccount(),
                transaction.getAmount(),
                transaction.getType(),
                false
        );

        // Apply new transaction to new account
        updateAccountBalance(newAccount, request.getAmount(), request.getType(), true);

        // Update transaction
        transaction.setAccount(newAccount);
        transaction.setCategory(newCategory);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());

        Transaction updated = transactionRepository.save(transaction);
        return transactionMapper.toResponse(updated);
    }

    /**
     * Delete transaction
     */
    @Transactional
    public void deleteTransaction(User user, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));

        // Reverse transaction from account balance
        updateAccountBalance(
                transaction.getAccount(),
                transaction.getAmount(),
                transaction.getType(),
                false
        );

        transactionRepository.delete(transaction);
    }

    /**
     * Get transaction summary (income, expenses, net balance)
     */
    public TransactionSummaryResponse getTransactionSummary(User user) {
        BigDecimal totalIncome = transactionRepository.calculateTotalIncome(user);
        BigDecimal totalExpenses = transactionRepository.calculateTotalExpenses(user);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        Long count = transactionRepository.countByUser(user);

        return new TransactionSummaryResponse(
                totalIncome,
                totalExpenses,
                netBalance,
                count
        );
    }

    /**
     * Get spending by category
     */
    public List<CategorySpendingResponse> getSpendingByCategory(User user) {
        List<Object[]> results = transactionRepository.getSpendingByCategory(user);

        return results.stream()
                .map(row -> new CategorySpendingResponse(
                        (Long) row[0],           // categoryId
                        (String) row[1],         // categoryName
                        (String) row[2],         // categoryIcon
                        (BigDecimal) row[3],     // totalAmount
                        (Long) row[4]            // transactionCount
                ))
                .collect(Collectors.toList());
    }

    /**
     * Helper: Update account balance based on transaction
     */
    private void updateAccountBalance(
            Account account,
            BigDecimal amount,
            TransactionType type,
            boolean isAdding
    ) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal newBalance;

        if (isAdding) {
            // Adding transaction
            if (type == TransactionType.CREDIT) {
                newBalance = currentBalance.add(amount);  // Income increases balance
            } else {
                newBalance = currentBalance.subtract(amount);  // Expense decreases balance
            }
        } else {
            // Reversing transaction (for update/delete)
            if (type == TransactionType.CREDIT) {
                newBalance = currentBalance.subtract(amount);
            } else {
                newBalance = currentBalance.add(amount);
            }
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
    }
}
