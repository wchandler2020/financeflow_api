package com.tiltedhat.financeflow_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDataResponse {
    private String merchantName;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
    private String receiptUrl;
    private String description;
}
