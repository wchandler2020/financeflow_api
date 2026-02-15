package com.tiltedhat.financeflow_backend.dto;

import com.tiltedhat.financeflow_backend.entity.Budget;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BudgetMapper {
    public Budget toEntity(CreateBudgetRequest request){
        Budget budget = new Budget();
        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());

        // User and Category will be set in the service layer
        return budget;
    }

    public BudgetResponse toResponse(Budget budget, BigDecimal spent){
        BigDecimal remaining = budget.getAmount().subtract(spent);

        // Calculate percentage - use double arithmetic for simplicity
        double percentageUsed = 0.0;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Convert to double first, then calculate
            double spentDouble = spent.doubleValue();
            double amountDouble = budget.getAmount().doubleValue();
            percentageUsed = (spentDouble / amountDouble) * 100.0;
        }

        // Debug logging
        System.out.println("Budget: " + budget.getCategory().getName());
        System.out.println("Amount: " + budget.getAmount());
        System.out.println("Spent: " + spent);
        System.out.println("Percentage: " + percentageUsed + "%");
        System.out.println("---");

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getCategory().getIcon(),
                budget.getAmount(),
                spent,
                remaining,
                percentageUsed,
                budget.getMonth(),
                budget.getYear(),
                budget.getCreatedAt()
        );
    }
}
