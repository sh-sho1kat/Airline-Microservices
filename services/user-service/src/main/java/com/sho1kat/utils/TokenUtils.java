package com.sho1kat.utils;

import com.sho1kat.enums.TokenType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;


public final class TokenUtils {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private TokenUtils() {
        // Prevent instantiation
    }

    public static String generateSecureToken() {
        byte[] bytes = new byte[32];

        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public static String hashToken(String rawToken) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    public static boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt == null
                || !expiresAt.isAfter(LocalDateTime.now());
    }

    public static Duration tokenLifetime(
            TokenType type,
            long emailVerificationHours,
            long passwordResetMinutes
    ) {
        return switch (type.name()) {
            case "EMAIL_VERIFICATION" ->
                    Duration.ofHours(emailVerificationHours);

            case "PASSWORD_RESET" ->
                    Duration.ofMinutes(passwordResetMinutes);

            default -> throw new IllegalArgumentException(
                    "Unsupported one-time token type: " + type
            );
        };
    }

    public static IllegalArgumentException invalidToken() {
        return new IllegalArgumentException(
                "Invalid, expired, or already-used token"
        );
    }
}