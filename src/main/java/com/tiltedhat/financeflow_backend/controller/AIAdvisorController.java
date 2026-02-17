package com.tiltedhat.financeflow_backend.controller;

import com.tiltedhat.financeflow_backend.dto.ChatRequest;
import com.tiltedhat.financeflow_backend.dto.ChatResponse;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.service.AIAdvisorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai-advisor")
@RequiredArgsConstructor
public class AIAdvisorController {

    private final AIAdvisorService aiAdvisorService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal User user
    ) {
        String response = aiAdvisorService.getFinancialAdvice(user, request.getMessage());

        return ResponseEntity.ok(new ChatResponse(
                response,
                UUID.randomUUID().toString() // Conversation ID for future use
        ));
    }
}