package com.tiltedhat.financeflow_backend.controller;

import com.tiltedhat.financeflow_backend.dto.*;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // POST /api/auth/register
    // register new user
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        String message =  authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(message));
    }

    // POST /api/login
    // Login user and get JWT token

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response =  authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // GET /api/auth/verify?token=ABC123
    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        String message =  authService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(message));
    }

    // POST /api/auth/resend-verification
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        String message = authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(message));
    }

}
