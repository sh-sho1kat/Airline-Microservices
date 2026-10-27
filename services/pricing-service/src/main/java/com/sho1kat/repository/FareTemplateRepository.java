package com.sho1kat.repository;

import com.sho1kat.entity.FareTemplate;
import com.sho1kat.enums.CabinClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FareTemplateRepository extends JpaRepository<FareTemplate, Long> {

    List<FareTemplate> findAllByFlightIdAndAirlineIdAndIsActiveTrue(Long flightId, Long airlineId);

    // Matches any active state; the service decides how to react to inactive duplicates.
    Optional<FareTemplate> findByFlightIdAndCabinAndFareClass(Long flightId, CabinClass cabin, String fareClass);

    List<FareTemplate> findAllByIsActiveTrue();
}
