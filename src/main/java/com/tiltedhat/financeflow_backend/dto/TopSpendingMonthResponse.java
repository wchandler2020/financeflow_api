package com.tiltedhat.financeflow_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopSpendingMonthResponse {
    private Integer month;
    private Integer year;
    private String monthName;
    private BigDecimal totalSpent;
}
