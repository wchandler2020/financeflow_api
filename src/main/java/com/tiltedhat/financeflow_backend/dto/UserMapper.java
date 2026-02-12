package com.tiltedhat.financeflow_backend.dto;

import com.tiltedhat.financeflow_backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

//    Converts RegisterRequest DTO to User
//    Used during registration

    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setCountry(request.getCountry());
        user.setTimezone(request.getTimezone());
        return user;
    }

    //    Converts User Entity to UserResponse
    //    Used when returning user to the frontend
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getCountry(),
                user.getTimezone(),
                user.isEmailVerified(),
                user.getCreatedDate()
        );
    }
}

//How These All Work Together:**
//**Registration Flow Example:**
// 1. Frontend sends JSON: { "email": "john@example.com", "username": "johndoe", "password": "password123", "firstName": "John", "lastName": "Doe" }
// ↓ 2. Controller receives RegisterRequest (validation happens)
// ↓ 3. Service checks: userRepository.existsByEmail(email)
// ↓ 4. Mapper converts: RegisterRequest → User entity
// ↓ 5. Service hashes password, generates token
// ↓ 6. Repository saves: userRepository.save(user)
// ↓ 7. Response: MessageResponse("Check email to verify")