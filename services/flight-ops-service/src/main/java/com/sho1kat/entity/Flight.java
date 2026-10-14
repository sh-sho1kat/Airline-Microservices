package com.sho1kat.entity;

import com.sho1kat.enums.FlightStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "flights",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_airline_flight_number",
                        columnNames = {"airline_id", "flight_number"}
                )
        },
        indexes = {
                @Index(name = "idx_flight_aircraft", columnList = "aircraft_id"),
                @Index(name = "idx_flight_departure", columnList = "departure_airport_id"),
                @Index(name = "idx_flight_arrival", columnList = "arrival_airport_id"),
                @Index(name = "idx_flight_status", columnList = "flight_status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, length = 20)
    private String flightNumber;

    @Column(name = "airline_id", nullable = false)
    private Long airlineId;

    @Column(name = "aircraft_id", nullable = false)
    private Long aircraftId;

    @Column(name = "departure_airport_id", nullable = false)
    private Long departureAirportId;

    @Column(name = "arrival_airport_id", nullable = false)
    private Long arrivalAirportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "flight_status", nullable = false, length = 20)
    private FlightStatus flightStatus;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}