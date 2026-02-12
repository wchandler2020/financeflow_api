package com.tiltedhat.financeflow_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(name = "user name")
    private String username;

    @Column(name = "first name")
    private String firstName;

    @Column(name = "last name")
    private String lastName;

    @Column(name = "country")
    private String country; // will be used for currency conversions

    @Column(name = "timezone")
    private String timezone;

    @Column(nullable = false)
    private String password; // this will be hashed in the mapper

    @Column(name = "email verification")
    private boolean emailVerified = false;

    @Column(name = "verification token")
    private String verificationToken;

    @Column(name = "token expired at")
    private LocalDateTime tokenExpiredAt;

    @Column(name = "token verified at")
    private LocalDateTime tokenVerifiedAt;

    @Enumerated(EnumType.STRING)
    private Role role =  Role.USER;

    @CreatedDate
    private LocalDateTime createdDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new  SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public boolean isVerificationTokenExpired() {
        return tokenExpiredAt != null && LocalDateTime.now().isAfter(tokenExpiredAt);
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override public boolean isAccountNonExpired() { return true; }

    @Override public boolean isAccountNonLocked() { return true; }

    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override public boolean isEnabled() { return true; }
}
