package com.tiltedhat.financeflow_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Get Authorization header from request
        final String authHeader = request.getHeader("Authorization");

        // 2. Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token, continue without authentication
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract JWT token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            // 4. Extract email from token
            userEmail = jwtUtil.extractUsername(jwt);

            // 5. If email exists and user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // 7. Validate token against user details
                if (jwtUtil.validateToken(jwt, userDetails)) {

                    // 8. Create authentication object
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // 9. Set additional details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 10. Set authentication in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token is invalid or expired - just continue without authentication
            // Spring Security will reject the request at the controller level
        }

        // 11. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}

//        What This Filter Does (Step by Step):**
//           Request Flow:
//        1. User sends request:
//           GET /api/accounts
//           Authorization: Bearer eyJhbGciOiJIUz...
//        ↓
//        2. Filter intercepts request
//        ↓
//        3. Extracts token from "Authorization" header
//        ↓
//        4. Validates token using JwtUtil
//        ↓
//        5. If valid → Load user from database
//        ↓
//        6. Create Authentication object
//        ↓
//        7. Set in SecurityContext
//        ↓
//        8. Controller receives authenticated user!
//        ↓
//        9. Controller can access: Authentication.getName() → user's email