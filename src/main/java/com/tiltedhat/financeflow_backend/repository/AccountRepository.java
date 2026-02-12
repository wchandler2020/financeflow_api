package com.tiltedhat.financeflow_backend.repository;

import com.tiltedhat.financeflow_backend.entity.Account;
import com.tiltedhat.financeflow_backend.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);
    List<Account> findByUserIdAndType(Long userId, AccountType type);
    //Find a specific account belonging to a user
    Optional<Account> findByIdAndUserId(Long id, Long userId);
    // Check if user has an account with this name
    boolean existsByUserIdAndName(Long userId,  String name);
    // Calculate total balance across all accounts for a user
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userId")
    BigDecimal getTotalBalanceByUserId(Long userId);
    // Get total balance by account type(example: total in all checking accounts)
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userid AND a.type = :type")
    BigDecimal getTotalBalanceByUserIdAndType(Long userid, AccountType type);

}
