package com.sho1kat.service;

import com.sho1kat.entity.RefreshToken;
import com.sho1kat.entity.User;
import com.sho1kat.entity.VerificationToken;
import com.sho1kat.enums.TokenType;

public interface TokenService {
    /** Creates and stores a refresh token; returns the RAW token to hand to the client. */
    String issueRefreshToken(User user);

    /** Validates the refresh token and revokes it (rotation). Throws 401 if invalid. */
    RefreshToken consumeRefreshToken(String rawToken);

    void revokeRefreshToken(String rawToken);

    void revokeAllRefreshTokens(User user);

    /** Creates an email-verification / password-reset token (invalidating older ones); returns RAW token. */
    String issueOneTimeToken(User user, TokenType type);

    /** Validates and marks the token as used. Throws 400 if invalid or expired. */
    VerificationToken consumeOneTimeToken(String rawToken, TokenType type);
}
