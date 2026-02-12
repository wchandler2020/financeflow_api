package com.tiltedhat.financeflow_backend.controller;

import com.tiltedhat.financeflow_backend.dto.CreateAccountRequest;
import com.tiltedhat.financeflow_backend.dto.UpdateAccountRequest;
import com.tiltedhat.financeflow_backend.dto.AccountResponse;
import com.tiltedhat.financeflow_backend.dto.MessageResponse;
import com.tiltedhat.financeflow_backend.entity.AccountType;
import com.tiltedhat.financeflow_backend.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * POST /api/accounts
     * Create a new account
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        AccountResponse response = accountService.createAccount(request, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * GET /api/accounts
     * Get all accounts for logged-in user
     * Optional: Filter by type with query param ?type=CHECKING
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(
            @RequestParam(required = false) AccountType type,
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<AccountResponse> accounts;
        if (type != null) {
            accounts = accountService.getAccountsByType(type, userEmail);
        } else {
            accounts = accountService.getAllAccounts(userEmail);
        }

        return ResponseEntity.ok(accounts);
    }

    /**
     * GET /api/accounts/{id}
     * Get specific account by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        AccountResponse response = accountService.getAccountById(id, userEmail);

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/accounts/{id}
     * Update account details (name, description)
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        AccountResponse response = accountService.updateAccount(id, request, userEmail);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/accounts/{id}
     * Delete an account
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteAccount(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        accountService.deleteAccount(id, userEmail);

        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }

    /**
     * GET /api/accounts/{id}/balance
     * Get current balance for specific account
     */
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        BigDecimal balance = accountService.getAccountBalance(id, userEmail);

        return ResponseEntity.ok(balance);
    }

    /**
     * GET /api/accounts/total-balance
     * Get total balance across all accounts
     */
    @GetMapping("/total-balance")
    public ResponseEntity<BigDecimal> getTotalBalance(Authentication authentication) {
        String userEmail = authentication.getName();
        BigDecimal total = accountService.getTotalBalance(userEmail);

        return ResponseEntity.ok(total);
    }

    /**
     * GET /api/accounts/total-balance/{type}
     * Get total balance by account type
     */
    @GetMapping("/total-balance/{type}")
    public ResponseEntity<BigDecimal> getTotalBalanceByType(
            @PathVariable AccountType type,
            Authentication authentication) {

        String userEmail = authentication.getName();
        BigDecimal total = accountService.getTotalBalanceByType(type, userEmail);

        return ResponseEntity.ok(total);
    }
}
