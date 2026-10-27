package com.sho1kat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "flight_instance_refs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ref_flight_instance",
                        columnNames = "flight_instance_id"
                )
        },
        indexes = {
                @Index(name = "idx_ref_flight", columnList = "flight_id"),
                @Index(name = "idx_ref_airline", columnList = "airline_id"),
                @Index(name = "idx_ref_departure", columnList = "departure_at"),
                @Index(name = "idx_ref_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FlightInstanceReference {

    // The primary key is the flight-ops instance ID; pricing shares Flight Ops ownership.
    @Id
    @Column(name = "flight_instance_id")
    private Long flightInstanceId;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Column(name = "airline_id", nullable = false)
    private Long airlineId;

    @Column(name = "departure_at", nullable = false)
    private LocalDateTime departureAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

}
