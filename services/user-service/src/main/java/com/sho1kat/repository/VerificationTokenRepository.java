package com.sho1kat.repository;

import com.sho1kat.entity.User;
import com.sho1kat.entity.VerificationToken;
import com.sho1kat.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenHashAndUsedAtIsNull(
            String tokenHash
    );

    void deleteByUser_IdAndUsedAtIsNull(Long userId);

    Optional<VerificationToken> findByTokenHashAndType(String tokenHash, TokenType type);

    List<VerificationToken> findAllByUserAndTypeAndUsedAtIsNull(User user, TokenType type);

    @Modifying
    @Query("update VerificationToken t set t.used = true "
            + "where t.user.id = :userId and t.type = :type and t.used = false")
    int invalidateAll(@Param("userId") Long userId, @Param("type") TokenType type);
}