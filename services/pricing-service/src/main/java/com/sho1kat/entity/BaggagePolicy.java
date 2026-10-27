package com.sho1kat.entity;

import com.sho1kat.enums.CabinClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "baggage_policies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_baggage_airline_cabin",
                        columnNames = {"airline_id", "cabin"}
                )
        },
        indexes = {
                @Index(name = "idx_baggage_airline", columnList = "airline_id"),
                @Index(name = "idx_baggage_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BaggagePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "airline_id", nullable = false)
    private Long airlineId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CabinClass cabin;

    @Column(name = "cabin_baggage_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal cabinBaggageKg;

    @Column(name = "checked_pieces", nullable = false)
    private int checkedPieces;

    @Column(name = "checked_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal checkedKg;

    @Column(name = "extra_kg_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal extraKgFee;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

}
