package com.tiltedhat.financeflow_backend.service;

import com.tiltedhat.financeflow_backend.dto.CreateAccountRequest;
import com.tiltedhat.financeflow_backend.dto.UpdateAccountRequest;
import com.tiltedhat.financeflow_backend.dto.AccountResponse;
import com.tiltedhat.financeflow_backend.entity.Account;
import com.tiltedhat.financeflow_backend.entity.AccountType;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.dto.AccountMapper;
import com.tiltedhat.financeflow_backend.repository.AccountRepository;
import com.tiltedhat.financeflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    /**
     * Create a new account for the logged-in user
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, String userEmail) {
        // Get the logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if account name already exists for this user
        if (accountRepository.existsByUserIdAndName(user.getId(), request.getName())) {
            throw new RuntimeException("Account with name '" + request.getName() + "' already exists");
        }

        // Convert DTO to Entity
        Account account = accountMapper.toEntity(request);
        account.setUser(user);  // Link to user

        // Save account
        Account savedAccount = accountRepository.save(account);

        // Convert to response DTO
        return accountMapper.toResponse(savedAccount);
    }

    /**
     * Get all accounts for the logged-in user
     */
    public List<AccountResponse> getAllAccounts(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Account> accounts = accountRepository.findByUserId(user.getId());

        return accounts.stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts by type (e.g., all checking accounts)
     */
    public List<AccountResponse> getAccountsByType(AccountType type, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Account> accounts = accountRepository.findByUserIdAndType(user.getId(), type);

        return accounts.stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific account by ID
     * Security: Ensures user can only access their own accounts
     */
    public AccountResponse getAccountById(Long accountId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found or access denied"));

        return accountMapper.toResponse(account);
    }

    /**
     * Update account details (name, description only - not balance)
     */
    @Transactional
    public AccountResponse updateAccount(Long accountId, UpdateAccountRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Security check: Make sure account belongs to user
        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found or access denied"));

        // Update only if new values provided
        if (request.getName() != null && !request.getName().isBlank()) {
            // Check if new name conflicts with existing account
            if (!account.getName().equals(request.getName()) &&
                    accountRepository.existsByUserIdAndName(user.getId(), request.getName())) {
                throw new RuntimeException("Account with name '" + request.getName() + "' already exists");
            }
            account.setName(request.getName());
        }

        if (request.getDescription() != null) {
            account.setDescription(request.getDescription());
        }

        Account updatedAccount = accountRepository.save(account);
        return accountMapper.toResponse(updatedAccount);
    }

    /**
     * Delete an account
     * TODO: Should check if account has transactions before deleting
     */
    @Transactional
    public void deleteAccount(Long accountId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Security check: Make sure account belongs to user
        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found or access denied"));

        // TODO: Check if account has transactions
        // If yes, either prevent deletion or soft-delete

        accountRepository.delete(account);
    }

    /**
     * Get current balance for a specific account
     */
    public BigDecimal getAccountBalance(Long accountId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found or access denied"));

        return account.getBalance();
    }

    /**
     * Get total balance across all accounts
     */
    public BigDecimal getTotalBalance(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        BigDecimal total = accountRepository.getTotalBalanceByUserId(user.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get total balance by account type
     */
    public BigDecimal getTotalBalanceByType(AccountType type, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        BigDecimal total = accountRepository.getTotalBalanceByUserIdAndType(user.getId(), type);
        return total != null ? total : BigDecimal.ZERO;
    }
}
