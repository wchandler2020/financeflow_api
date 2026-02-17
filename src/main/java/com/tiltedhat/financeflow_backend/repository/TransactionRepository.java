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

    List<Transaction> findTop10ByUserOrderByTransactionDateDesc(User user);

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

    // Calculate total spending for a category in a specific month/year
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user = :user " +
            "AND t.category.id = :categoryId " +
            "AND t.type = 'DEBIT' " +
            "AND MONTH(t.transactionDate) = :month " +
            "AND YEAR(t.transactionDate) = :year")
    BigDecimal calculateSpendingForCategoryInMonth(
            @Param("user") User user,
            @Param("categoryId") Long categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    // Get top spending months
    @Query("SELECT " +
            "YEAR(t.transactionDate) as year, " +
            "MONTH(t.transactionDate) as month, " +
            "SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE t.user = :user " +
            "AND t.type = 'DEBIT' " +
            "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
            "ORDER BY SUM(t.amount) DESC")
    List<Object[]> getTopSpendingMonths(@Param("user") User user);

    // Get category spending over time
    @Query("SELECT " +
            "YEAR(t.transactionDate) as year, " +
            "MONTH(t.transactionDate) as month, " +
            "t.category.id as categoryId, " +
            "t.category.name as categoryName, " +
            "SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE t.user = :user " +
            "AND t.type = 'DEBIT' " +
            "AND t.transactionDate >= :startDate " +
            "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate), t.category.id, t.category.name " +
            "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(t.amount) DESC")
    List<Object[]> getCategorySpendingOverTime(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate
    );

    // Get monthly spending trends (last N months)
    @Query("SELECT " +
            "YEAR(t.transactionDate) as year, " +
            "MONTH(t.transactionDate) as month, " +
            "SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END) as expenses " +
            "FROM Transaction t " +
            "WHERE t.user = :user " +
            "AND t.transactionDate >= :startDate " +
            "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
            "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> getMonthlyTrends(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate
    );
}
