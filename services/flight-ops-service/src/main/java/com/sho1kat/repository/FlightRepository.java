package com.sho1kat.repository;

import com.sho1kat.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {

    Optional<Flight> findByFlightNumberIgnoreCase(String flightNumber);

    boolean existsByFlightNumberIgnoreCase(String flightNumber);

    boolean existsByFlightNumberIgnoreCaseAndIdNot(String flightNumber, Long id);

    Optional<Flight> findByAirlineIdAndFlightNumberIgnoreCase(Long airlineId, String flightNumber);
}