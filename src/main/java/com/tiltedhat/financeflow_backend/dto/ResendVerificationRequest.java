package com.tiltedhat.financeflow_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationRequest {
    @Email(message = "Email is required.")
    @NotBlank(message = "Email is required")
    private String email;
}
