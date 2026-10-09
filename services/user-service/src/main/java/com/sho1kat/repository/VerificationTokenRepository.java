package com.sho1kat.repository;

import com.sho1kat.entity.User;
import com.sho1kat.entity.VerificationToken;
import com.sho1kat.enums.TokenType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenHashAndType(String tokenHash, TokenType type);

    @Modifying(flushAutomatically = true)
    @Query("""
        update VerificationToken t
        set t.usedAt = :usedAt
        where t.user.id = :userId
          and t.type = :type
          and t.usedAt is null
        """)
    int invalidateAll(
            @Param("userId") Long userId,
            @Param("type") TokenType type,
            @Param("usedAt") Instant usedAt
    );

    @Modifying(flushAutomatically = true)
    @Query("update VerificationToken t set t.usedAt = :usedAt where t.id = :id and t.usedAt is null")
    int markUsedIfActive(@Param("id") Long id, @Param("usedAt") Instant usedAt);
}