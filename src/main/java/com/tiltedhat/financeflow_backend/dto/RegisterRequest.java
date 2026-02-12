package com.tiltedhat.financeflow_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email(message = "Valid email address is required.")
    @NotBlank(message = "Email is required.")
    private String email;

    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 30)
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required.")
    @Size(max = 30, message = "first name cannot exceed 30 characters")
    private String firstName;

    @NotBlank(message = "last name is required.")
    @Size(max = 30, message = "last name cannot exceed 30 characters")
    private String lastName;

    @Size(max=100, message = "Your country name cannot exceed 100 characters")
    private String country;

    @Size(max=100, message = "Your timezone cannot exceed 100 characters")
    private String timezone;



}
