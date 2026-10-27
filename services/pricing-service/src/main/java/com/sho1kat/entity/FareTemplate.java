package com.sho1kat.entity;

import com.sho1kat.enums.CabinClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        name = "fare_templates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_template_flight_cabin_class",
                        columnNames = {"flight_id", "cabin", "fare_class"}
                )
        },
        indexes = {
                @Index(name = "idx_template_airline", columnList = "airline_id"),
                @Index(name = "idx_template_flight", columnList = "flight_id"),
                @Index(name = "idx_template_rule", columnList = "fare_rule_id"),
                @Index(name = "idx_template_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FareTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "airline_id", nullable = false)
    private Long airlineId;

    // Owned by flight-ops-service; deliberately an ID, not a cross-service JPA relation.
    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CabinClass cabin;

    @Column(name = "fare_class", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String fareClass;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currency = "BDT";

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fare_rule_id", nullable = false, foreignKey = @ForeignKey(name = "fk_template_fare_rule"))
    private FareRule fareRule;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

}
