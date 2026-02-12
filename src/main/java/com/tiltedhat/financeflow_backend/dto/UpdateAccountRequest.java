package com.tiltedhat.financeflow_backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAccountRequest {
    @Size(max = 100, message = "Account name account exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    //Balances will not be changed/updated here this will happen through transactions
}
