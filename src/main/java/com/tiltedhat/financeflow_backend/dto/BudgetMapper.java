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

        Double percentageUsed = 0.0;
        if(remaining.compareTo(BigDecimal.ZERO) <= 0){
            percentageUsed = spent
                    .divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

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
