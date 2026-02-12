package com.tiltedhat.financeflow_backend.dto;

import com.tiltedhat.financeflow_backend.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotNull(message = "Initial Balance is required")
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private BigDecimal balance;

    @Size(min = 3, max = 3, message = "Currency must be a 3 letter ISO code like USD")
    private String currency;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
