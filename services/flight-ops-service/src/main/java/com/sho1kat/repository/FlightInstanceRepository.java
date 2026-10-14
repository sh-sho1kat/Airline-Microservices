package com.sho1kat.repository;

import com.sho1kat.entity.FlightInstance;
import com.sho1kat.enums.FlightStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface FlightInstanceRepository
        extends JpaRepository<FlightInstance, Long> {

    @Query("""
        SELECT fi
        FROM FlightInstance fi
        WHERE (:airlineId IS NULL OR fi.airlineId = :airlineId)
          AND (:departureAirportId IS NULL
               OR fi.departureAirportId = :departureAirportId)
          AND (:arrivalAirportId IS NULL
               OR fi.arrivalAirportId = :arrivalAirportId)
          AND (:flightId IS NULL OR fi.flight.id = :flightId)
          AND (:dayStart IS NULL
               OR fi.departureDateTime >= :dayStart)
          AND (:dayEnd IS NULL
               OR fi.departureDateTime < :dayEnd)
          AND (:status IS NULL OR fi.status = :status)
          AND (:isActive IS NULL OR fi.isActive = :isActive)
        """)
    Page<FlightInstance> searchFlightInstances(
            @Param("airlineId") Long airlineId,
            @Param("departureAirportId") Long departureAirportId,
            @Param("arrivalAirportId") Long arrivalAirportId,
            @Param("flightId") Long flightId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("status") FlightStatus status,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}