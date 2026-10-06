package com.sho1kat.repository;

import com.sho1kat.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(
            String tokenHash
    );

    void deleteByUser_IdAndUsedAtIsNull(
            Long userId
    );
}