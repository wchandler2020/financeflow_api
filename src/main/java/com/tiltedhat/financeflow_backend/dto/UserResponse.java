package com.tiltedhat.financeflow_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String country;
    private String timezone;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
