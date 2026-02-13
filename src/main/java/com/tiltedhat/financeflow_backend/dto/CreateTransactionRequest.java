package com.tiltedhat.financeflow_backend.dto;

import com.tiltedhat.financeflow_backend.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTransactionRequest {
    @NotNull(message = "Account ID is required")
    private long accountId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type; //DEBIT or CREDIT

    @Size(max = 500, message = "Description cannot exceed 500 character")
    private String description;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

}
