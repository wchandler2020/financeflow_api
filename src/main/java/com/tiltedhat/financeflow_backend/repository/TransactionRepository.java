package com.tiltedhat.financeflow_backend.repository;

import com.tiltedhat.financeflow_backend.entity.Account;
import com.tiltedhat.financeflow_backend.entity.Category;
import com.tiltedhat.financeflow_backend.entity.Transaction;
import com.tiltedhat.financeflow_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions for a user
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    // Find transaction by ID and user (for permission check)
    Optional<Transaction> findByIdAndUser(Long id, User user);

    // Find by account
    List<Transaction> findByAccountOrderByTransactionDateDesc(Account account);

    // Find by category
    List<Transaction> findByCategoryOrderByTransactionDateDesc(Category category);

    // Find by date range
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find by account and date range
    @Query("SELECT t FROM Transaction t WHERE t.account = :account " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountAndDateRange(
            @Param("account") Account account,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Calculate total income for user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user = :user AND t.type = 'CREDIT'")
    BigDecimal calculateTotalIncome(@Param("user") User user);

    // Calculate total expenses for user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user = :user AND t.type = 'DEBIT'")
    BigDecimal calculateTotalExpenses(@Param("user") User user);

    // Get spending by category
    @Query("SELECT t.category.id, t.category.name, t.category.icon, " +
            "SUM(t.amount), COUNT(t) " +
            "FROM Transaction t " +
            "WHERE t.user = :user AND t.type = 'DEBIT' " +
            "GROUP BY t.category.id, t.category.name, t.category.icon " +
            "ORDER BY SUM(t.amount) DESC")
    List<Object[]> getSpendingByCategory(@Param("user") User user);

    Long countByUser(User user);
}
