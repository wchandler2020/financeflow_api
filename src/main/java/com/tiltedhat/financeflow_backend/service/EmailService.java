package com.tiltedhat.financeflow_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from.email}")
    private String fromEmail;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationLink = frontendUrl + "/#/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify Your FinanceFlow Account");
        message.setText(
                "Hi,\n\n" +
                        "Thank you for registering with FinanceFlow!\n\n" +
                        "Please verify your email by clicking the link below:\n\n" +
                        verificationLink + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "Best regards,\nThe FinanceFlow Team"
        );

        try {
            mailSender.send(message);
            System.out.println("✅ Verification email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to: " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}

