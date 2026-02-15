package com.tiltedhat.financeflow_backend.service;

import com.tiltedhat.financeflow_backend.dto.CategoryTrendResponse;
import com.tiltedhat.financeflow_backend.dto.MonthlyTrendResponse;
import com.tiltedhat.financeflow_backend.dto.TopSpendingMonthResponse;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    /**
     * Get monthly income/expense trends for the last N months
     */
    public List<MonthlyTrendResponse> getMonthlyTrends(User user, Integer months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);

        List<Object[]> results = transactionRepository.getMonthlyTrends(user, startDate);

        return results.stream()
                .map(row -> {
                    Integer year = (Integer) row[0];
                    Integer month = (Integer) row[1];
                    BigDecimal income = (BigDecimal) row[2];
                    BigDecimal expenses = (BigDecimal) row[3];
                    BigDecimal net = income.subtract(expenses);

                    String monthName = Month.of(month).name();
                    monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();

                    return new MonthlyTrendResponse(
                            year,
                            month,
                            monthName,
                            income,
                            expenses,
                            net
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Get category spending over time
     */
    public List<CategoryTrendResponse> getCategorySpendingOverTime(User user, Integer months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);

        List<Object[]> results = transactionRepository.getCategorySpendingOverTime(user, startDate);

        return results.stream()
                .map(row -> {
                    Integer year = (Integer) row[0];
                    Integer month = (Integer) row[1];
                    Long categoryId = (Long) row[2];
                    String categoryName = (String) row[3];
                    BigDecimal amount = (BigDecimal) row[4];

                    String monthName = Month.of(month).name();
                    monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();

                    return new CategoryTrendResponse(
                            year,
                            month,
                            monthName,
                            categoryId,
                            categoryName,
                            amount
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Get top spending months
     */
    public List<TopSpendingMonthResponse> getTopSpendingMonths(User user, Integer limit) {
        List<Object[]> results = transactionRepository.getTopSpendingMonths(user);

        return results.stream()
                .limit(limit)
                .map(row -> {
                    Integer year = (Integer) row[0];
                    Integer month = (Integer) row[1];
                    BigDecimal totalSpent = (BigDecimal) row[2];

                    String monthName = Month.of(month).name();
                    monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();

                    return new TopSpendingMonthResponse(
                            year,
                            month,
                            monthName + " " + year,
                            totalSpent
                    );
                })
                .collect(Collectors.toList());
    }
}
