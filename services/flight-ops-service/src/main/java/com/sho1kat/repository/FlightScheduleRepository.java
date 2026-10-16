package com.sho1kat.repository;

import com.sho1kat.entity.FlightSchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, Long> {

    @EntityGraph(attributePaths = "flight")
    List<FlightSchedule> findByFlight_AirlineId(Long airlineId);

    @EntityGraph(attributePaths = "flight")
    Optional<FlightSchedule> findByIdAndFlight_AirlineId(Long id, Long airlineId);

    boolean existsByFlight_Id(Long flightId);
}