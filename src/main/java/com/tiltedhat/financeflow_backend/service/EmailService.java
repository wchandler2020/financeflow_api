package com.tiltedhat.financeflow_backend.service;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Value("${mailtrap.api-token}")
    private String apiToken;

    @Value("${mailtrap.inbox-id}")
    private Long inboxId;

    @Value("${mail.from.email}")
    private String fromEmail;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationLink = frontendUrl + "/#/verify?token=" + token;

        try {
            final MailtrapConfig config = new MailtrapConfig.Builder()
                    .sandbox(true)
                    .inboxId(inboxId)
                    .token(apiToken)
                    .build();

            final MailtrapClient client = MailtrapClientFactory.createMailtrapClient(config);

            final MailtrapMail mail = MailtrapMail.builder()
                    .from(new Address(fromEmail, fromName))
                    .to(List.of(new Address(toEmail)))
                    .subject("Verify Your FinanceFlow Account")
                    .text(
                            "Hi,\n\n" +
                                    "Thank you for registering with FinanceFlow!\n\n" +
                                    "Please verify your email by clicking the link below:\n\n" +
                                    verificationLink + "\n\n" +
                                    "This link expires in 24 hours.\n\n" +
                                    "Best regards,\nThe FinanceFlow Team"
                    )
                    .build();

            System.out.println(client.send(mail));
            System.out.println("✅ Verification email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email to: " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}

