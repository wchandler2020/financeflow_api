package com.tiltedhat.financeflow_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryTrendResponse {
    private Integer year;
    private Integer month;
    private String monthName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
}
