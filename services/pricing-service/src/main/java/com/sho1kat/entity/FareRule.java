package com.sho1kat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fare_rules")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // Refundable means cancellation is permitted; refundPercent 0 then allows no cash refund.
    @Column(nullable = false)
    private boolean refundable;

    @Column(name = "refund_percent", nullable = false)
    private int refundPercent;

    @Column(name = "change_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal changeFee = BigDecimal.ZERO;

    @Column(name = "min_advance_days", nullable = false)
    private int minAdvanceDays;

    @Column(name = "max_stay_days")
    private Integer maxStayDays;

    @Column(name = "cancel_cutoff_hours", nullable = false)
    private int cancelCutoffHours;

    @Column(name = "no_show_penalty", nullable = false, precision = 12, scale = 2)
    private BigDecimal noShowPenalty = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

}
