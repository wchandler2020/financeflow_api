package com.tiltedhat.financeflow_backend.controller;

import com.tiltedhat.financeflow_backend.dto.ReceiptDataResponse;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/scan")
    public ResponseEntity<ReceiptDataResponse> scanReceipt(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ReceiptDataResponse data = receiptService.processReceipt(file, user.getId());
            return ResponseEntity.ok(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
