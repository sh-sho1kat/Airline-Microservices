package com.sho1kat.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/*
 * Stateless JWT verification for Pricing Service.
 *
 * user-service signs the tokens, so this service never loads users from a database;
 * it only verifies the signature and reads the subject and roles claims.
 */
@Service
public class JwtProvider {

    // The exact same secret/encoding that user-service uses to sign JWTs.
    @Value("${app.jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public boolean isTokenValid(String token) {

        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public String extractSubject(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {

        Object roles = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles");

        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(role -> role instanceof String)
                    .map(role -> (String) role)
                    .toList();
        }

        if (roles instanceof String single) {
            return List.of(single);
        }

        return List.of();
    }
}
