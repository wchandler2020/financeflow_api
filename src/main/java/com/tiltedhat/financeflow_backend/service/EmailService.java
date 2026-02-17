package com.tiltedhat.financeflow_backend.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${mail.from.email}")
    private String fromEmail;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationLink = frontendUrl + "/#/verify?token=" + token;

        try {
            Resend resend = new Resend(apiKey);

            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromName + " <" + fromEmail + ">")
                    .to(toEmail)
                    .subject("Verify Your FinanceFlow Account")
                    .html(
                            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                                    "<h2 style='color: #6366f1;'>Welcome to FinanceFlow!</h2>" +
                                    "<p>Thank you for registering. Please verify your email by clicking below:</p>" +
                                    "<a href='" + verificationLink + "' " +
                                    "style='background-color: #6366f1; color: white; padding: 12px 24px; " +
                                    "text-decoration: none; border-radius: 6px; display: inline-block; margin: 16px 0;'>" +
                                    "Verify Email Address</a>" +
                                    "<p>Or copy this link: <a href='" + verificationLink + "'>" + verificationLink + "</a></p>" +
                                    "<p style='color: #666; font-size: 14px;'>This link expires in 24 hours.</p>" +
                                    "<p style='color: #666; font-size: 14px;'>If you did not create this account, ignore this email.</p>" +
                                    "<hr style='border: none; border-top: 1px solid #eee; margin: 24px 0;'/>" +
                                    "<p style='color: #999; font-size: 12px;'>The FinanceFlow Team</p>" +
                                    "</div>"
                    )
                    .build();

            CreateEmailResponse response = resend.emails().send(options);
            System.out.println("✅ Verification email sent to: " + toEmail);
            System.out.println("Email ID: " + response.getId());

        } catch (Exception e) {
            System.err.println("❌ Failed to send email to: " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}