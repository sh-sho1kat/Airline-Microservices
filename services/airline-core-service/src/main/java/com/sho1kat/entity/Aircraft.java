package com.sho1kat.entity;

import com.sho1kat.enums.AircraftStatus;
import com.sho1kat.enums.AirlineStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "aircraft",
        uniqueConstraints = @UniqueConstraint(name = "aircraft_code", columnNames = "code"),
        indexes = {
                @Index(name = "idx_aircraft_airline_id", columnList = "airline_id"),
                @Index(name = "idx_aircraft_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aircraft {

    /** Days before nextMaintenanceDate at which an aircraft is flagged as maintenance-due. */
    public static final int MAINTENANCE_WARNING_WEEKS = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "airline_id", nullable = false)
    private Airline airline;

    /** Fleet code / tail number, globally unique, stored upper-case (e.g. "S2-AJA"). */
    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, length = 50)
    private String manufacturer;

    @Column(name = "seating_capacity", nullable = false)
    private Integer seatingCapacity;

    @Column(name = "economy_seats", nullable = false)
    @Builder.Default
    private Integer economySeats = 0;

    @Column(name = "premium_economy_seats", nullable = false)
    @Builder.Default
    private Integer premiumEconomySeats = 0;

    @Column(name = "business_seats", nullable = false)
    @Builder.Default
    private Integer businessSeats = 0;

    @Column(name = "first_class_seats", nullable = false)
    @Builder.Default
    private Integer firstClassSeats = 0;

    @Column(name = "range_km")
    private Integer rangeKm;

    @Column(name = "cruising_speed_kmh")
    private Integer cruisingSpeedKmh;

    @Column(name = "max_altitude_ft")
    private Integer maxAltitudeFt;

    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AircraftStatus status = AircraftStatus.ACTIVE;

    /** Manual availability flag. Forced to false whenever status is not ACTIVE. */
    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = Boolean.TRUE;

    @LastModifiedBy
    @Column(name = "updated_by_id")
    private Long updatedById;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public int getTotalSeats() {
        return nz(economySeats) + nz(premiumEconomySeats) + nz(businessSeats) + nz(firstClassSeats);
    }

    /**
     * The one flag other services should use to decide whether this aircraft can be scheduled:
     * aircraft ACTIVE and available, and its airline ACTIVE. Derived, never stored.
     */
    public boolean isOperational() {
        return AircraftStatus.ACTIVE.equals(status)
                && Boolean.TRUE.equals(isAvailable)
                && airline != null
                && AirlineStatus.ACTIVE.equals(airline.getStatus());
    }

    public boolean requiresMaintenance() {
        return nextMaintenanceDate != null
                && nextMaintenanceDate.isBefore(LocalDate.now().plusWeeks(MAINTENANCE_WARNING_WEEKS));
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
