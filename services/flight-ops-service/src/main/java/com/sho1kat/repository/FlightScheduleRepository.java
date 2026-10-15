package com.sho1kat.repository;

import com.sho1kat.entity.FlightSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightScheduleRepository
        extends JpaRepository<FlightSchedule, Long> {

    List<FlightSchedule> findByFlight_AirlineId(Long airlineId);

    Optional<FlightSchedule> findByIdAndFlight_AirlineId(
            Long id,
            Long airlineId
    );

}