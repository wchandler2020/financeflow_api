package com.tiltedhat.financeflow_backend.service;


import com.tiltedhat.financeflow_backend.dto.AuthResponse;
import com.tiltedhat.financeflow_backend.dto.LoginRequest;
import com.tiltedhat.financeflow_backend.dto.RegisterRequest;
import com.tiltedhat.financeflow_backend.dto.UserMapper;
import com.tiltedhat.financeflow_backend.entity.Role;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.repository.UserRepository;
import com.tiltedhat.financeflow_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

//  1.Check if email exists
//  2. Create user with hashed password
//  3. Generate verification token
//  4. Save user
//  5. Send verification email TODO: implement email service

    @Transactional
    public String register(RegisterRequest request) {
        //Check if email already exists
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email is already in use");
        }

        //Check if the username already exists
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("Username is already in use");
        }

        //Convert DTO to Entity
        User user = userMapper.toEntity(request);

        //Hash password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        //Set Default Role
        user.setRole(Role.USER);

        //Generate verification token
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiredAt(LocalDateTime.now().plusHours(24)); // this will set the token expiration
        user.setEmailVerified(true);

        //save the user
        userRepository.save(user);

        // send email verification email
        emailService.sendVerificationEmail(user.getEmail(), token);

        return "Registration successful. Please check your email to verify your email address.";
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        //Load the user from the db
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        //Check if the email is verified
        if(!user.isEmailVerified()){
            throw new RuntimeException("Please verify your email before logging in.");
        }

        //Genetate token

        String token = jwtUtil.generateToken(user);
        System.out.println("FULLNAME: " + user.getFullName());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName()
        );
    }

    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerificationTokenExpired()) {
            throw new RuntimeException("Verification token is expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiredAt(null);
        user.setTokenVerifiedAt(LocalDateTime.now());

        userRepository.save(user);
        return "Email verified successfully, you can now log in.";
    }

    @Transactional
    public String resendVerificationEmail(String email) {
        //find user by email or throw exception
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already verified
        if(user.isEmailVerified()){
            throw new RuntimeException("Email already verified");
        }

        // if not verified generate new token
        String token  = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiredAt(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        // TODO: Send Verfication email
        emailService.sendVerificationEmail(user.getEmail(), token);
        return "Verification email sent, please check your inbox.";
    }

}
