package com.sho1kat.repository;

import com.sho1kat.entity.FlightInstance;
import com.sho1kat.enums.FlightStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightInstanceRepository extends JpaRepository<FlightInstance, Long> {

    @EntityGraph(attributePaths = "flight")
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

    @Override
    @EntityGraph(attributePaths = "flight")
    Page<FlightInstance> findAll(Pageable pageable);

    boolean existsByFlight_IdAndDepartureDateTime(
            Long flightId,
            LocalDateTime departureDateTime
    );

    boolean existsBySchedule_Id(Long scheduleId);

    boolean existsByFlight_Id(Long flightId);

    List<FlightInstance> findBySchedule_IdAndDepartureDateTimeAfter(
            Long scheduleId,
            LocalDateTime after
    );

    // Pass -1L as excludeId when the instance is not stored yet.
    @Query("""
        SELECT CASE WHEN COUNT(fi) > 0 THEN true ELSE false END
        FROM FlightInstance fi
        WHERE fi.aircraftId = :aircraftId
          AND fi.status <> :ignoredStatus
          AND fi.id <> :excludeId
          AND fi.departureDateTime < :windowEnd
          AND fi.arrivalDateTime > :windowStart
        """)
    boolean existsAircraftConflict(
            @Param("aircraftId") Long aircraftId,
            @Param("ignoredStatus") FlightStatus ignoredStatus,
            @Param("excludeId") Long excludeId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd
    );
}