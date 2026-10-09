package com.sho1kat.service.impl;

import com.sho1kat.service.MailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.app.mail-from}")
    private String mailFrom;

    @Value("${spring.tokens.email-verification-hours}")
    private final int emailVerificationHours;

    @Value("${spring.tokens.password-reset-minutes}")
    private final int passwordResetMinutes;

    @Override
    public void sendVerificationEmail(String to, String Name, String token) {
        String verificationLink = buildFrontendLink(
                "/verify-email",
                token
        );

        String body = """
                Hello %s,
                
                Welcome to Airline!
                
                Please verify your email address by opening the link below:
                %s
                
                This verification link expires in %d hours.

                Regards,
                Airline Team
                """.formatted(
                getName(Name),
                verificationLink,
                emailVerificationHours
        );

        sendMail(to, "Verify Your Airline Account", body);
    }


    @Override
    public void sendPasswordResetEmail(String to, String Name, String token) {
        String resetLink = buildFrontendLink(
                "/reset-password",
                token
        );

        String body = """
                Hello %s,
                
                We received a request to reset your Airline account password.
                
                Open the following link to reset your password:
                %s

                This password reset link expires in %d minutes.

                Regards,
                Airline Team
                """.formatted(
                getName(Name),
                resetLink,
                passwordResetMinutes
        );

        sendMail(to,"Reset Your Airline Password", body);
    }


    @Override
    public void sendStaffApprovedEmail(String to, String Name) {
        String body = """
                Hello %s,

                Good news! Your staff application for Airline has been approved.

                You can now sign in and access the features available to your account.

                Thank you for joining Airline.

                Regards,
                Airline Team
                """.formatted(getName(Name));

        sendMail(to, "Your Airline Staff Application Has Been Approved", body);
    }


    @Override
    public void sendStaffRejectedEmail(String to, String firstName, String reason) {
        String rejectionReason =
                reason == null || reason.isBlank()
                        ? "No additional reason was provided."
                        : reason.trim();

        String body = """
                Hello %s,

                Thank you for your interest in joining the Airline staff.

                Unfortunately, your staff application has not been approved.

                Reason:
                %s

                If you believe this decision was made in error, please contact the Airline administration team.

                Regards,
                Airline Team
                """.formatted(
                getName(firstName),
                rejectionReason
        );

        sendMail(to, "Update on Your Airline Staff Application", body);
    }


    @Override
    public void sendMail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email must not be empty"
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }


    private String buildFrontendLink(String path, String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Token must not be empty"
            );
        }

        String baseUrl = frontendUrl.replaceAll("/+$", "");

        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);

        return baseUrl + path + "?token=" + encodedToken;
    }


    private String getName(String Name) {
        return Name == null || Name.isBlank()
                ? "there"
                : Name.trim();
    }
}