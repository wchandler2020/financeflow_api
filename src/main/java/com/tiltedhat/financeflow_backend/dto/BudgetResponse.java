package com.tiltedhat.financeflow_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BudgetResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private BigDecimal amount;
    private BigDecimal spent;
    private BigDecimal remaining;
    private Double percentageUsed;
    private Integer month;
    private Integer year;
    private LocalDateTime createAt;
}
