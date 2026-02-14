package com.tiltedhat.financeflow_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String username;
    private String fullname;

    public AuthResponse(String token, Long id, String email, String username, String fullName) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.username = username;
        this.fullname = fullName;
    }
}
