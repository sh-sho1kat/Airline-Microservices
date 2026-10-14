package com.sho1kat.entity;

import com.sho1kat.enums.FlightStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "flight_instances",
        indexes = {
                @Index(name = "idx_instance_airline", columnList = "airline_id"),
                @Index(name = "idx_instance_flight", columnList = "flight_id"),
                @Index(name = "idx_instance_schedule", columnList = "schedule_id"),
                @Index(name = "idx_instance_departure", columnList = "departure_date_time"),
                @Index(name = "idx_instance_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FlightInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "airline_id", nullable = false)
    private Long airlineId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "departure_airport_id", nullable = false)
    private Long departureAirportId;

    @Column(name = "arrival_airport_id", nullable = false)
    private Long arrivalAirportId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "departure_date_time", nullable = false)
    private LocalDateTime departureDateTime;

    @Column(name = "arrival_date_time", nullable = false)
    private LocalDateTime arrivalDateTime;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FlightStatus status = FlightStatus.SCHEDULED;

    @Column(name = "min_advance_booking_days")
    private Integer minAdvanceBookingDays;

    @Column(name = "max_advance_booking_days")
    private Integer maxAdvanceBookingDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedBy
    private Instant updatedAt;

    @Transient
    public String getFormattedDuration() {
        if (departureDateTime == null || arrivalDateTime == null) {
            return null;
        }

        Duration duration = Duration.between(
                departureDateTime,
                arrivalDateTime
        );

        if (duration.isNegative()) {
            return null;
        }

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours).append(" h ");
        }

        if (minutes > 0) {
            result.append(minutes).append(" min");
        }

        if (result.isEmpty()) {
            return "0 min";
        }

        return result.toString().trim();
    }

}