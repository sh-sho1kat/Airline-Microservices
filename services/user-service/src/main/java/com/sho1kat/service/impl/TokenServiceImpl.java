package com.sho1kat.service.impl;

import com.sho1kat.entity.RefreshToken;
import com.sho1kat.entity.User;
import com.sho1kat.entity.VerificationToken;
import com.sho1kat.enums.TokenType;
import com.sho1kat.repository.RefreshTokenRepository;
import com.sho1kat.repository.VerificationTokenRepository;
import com.sho1kat.service.TokenService;
import com.sho1kat.utils.TokenUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.sho1kat.utils.TokenUtils.*;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${spring.jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    @Value("${spring.tokens.email-verification-hours}")
    private long emailVerificationHours;

    @Value("${spring.tokens.password-reset-minutes}")
    private long passwordResetMinutes;

    @Override
    public String issueRefreshToken(User user) {
        String rawToken = generateSecureToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenUtils.hashToken(rawToken))
                .expiresAt(
                        Instant.now().plus(
                                refreshTokenExpirationDays,
                                ChronoUnit.DAYS
                        )
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Override
    public RefreshToken consumeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw invalidToken();
        }

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(TokenUtils::invalidToken);

        Instant now = Instant.now();

        if (refreshToken.isRevoked()
                || refreshToken.getExpiresAt() == null
                || !refreshToken.getExpiresAt().isAfter(now)) {

            throw invalidToken();
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        refreshTokenRepository
                .findByTokenHash(TokenUtils.hashToken(rawToken))
                .ifPresent(refreshToken -> {

                    if (!refreshToken.isRevoked()) {
                        refreshToken.setRevoked(true);
                        refreshTokenRepository.save(refreshToken);
                    }
                });
    }

    @Override
    public void revokeAllRefreshTokens(User user) {
        List<RefreshToken> activeTokens =
                refreshTokenRepository
                        .findAllByUserAndRevokedFalse(user);

        activeTokens.forEach(
                token -> token.setRevoked(true)
        );

        refreshTokenRepository.saveAll(activeTokens);
    }

    @Override
    public String issueOneTimeToken(User user, TokenType type) {
        // Invalidate previous unused tokens of this type.
        verificationTokenRepository.invalidateAll(user.getId(), type);

        String rawToken = generateSecureToken();

        Instant expiresAt = Instant.now().plus(
                TokenUtils.tokenLifetime(
                        type,
                        emailVerificationHours,
                        passwordResetMinutes
                )
        );

        VerificationToken verificationToken =
                VerificationToken.builder()
                        .user(user)
                        .tokenHash(
                                TokenUtils.hashToken(rawToken)
                        )
                        .type(type)
                        .expiresAt(expiresAt)
                        .build();

        verificationTokenRepository.save(verificationToken);

        // Return raw token for the verification/reset link.
        return rawToken;
    }

    @Override
    @Transactional
    public VerificationToken consumeOneTimeToken(String rawToken, TokenType type) {
        if (rawToken == null
                || rawToken.isBlank()
                || type == null) {

            throw invalidToken();
        }

        String tokenHash = TokenUtils.hashToken(rawToken);

        VerificationToken verificationToken =
                verificationTokenRepository
                        .findByTokenHashAndType(
                                tokenHash,
                                type
                        )
                        .orElseThrow(TokenUtils::invalidToken);

        Instant now = Instant.now();

        if (verificationToken.getUsedAt() != null
                || verificationToken.getExpiresAt() == null
                || !verificationToken.getExpiresAt()
                .isAfter(now)) {

            throw invalidToken();
        }

        // Mark the token as consumed.
        verificationToken.setUsedAt(now);

        return verificationTokenRepository
                .save(verificationToken);
    }
}
