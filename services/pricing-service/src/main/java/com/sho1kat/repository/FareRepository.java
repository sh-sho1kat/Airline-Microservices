package com.sho1kat.repository;

import com.sho1kat.entity.Fare;
import com.sho1kat.enums.CabinClass;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FareRepository extends JpaRepository<Fare, Long> {

    @EntityGraph(attributePaths = "fareRule")
    List<Fare> findAllByFlightInstanceIdAndIsActiveTrue(Long flightInstanceId);

    @EntityGraph(attributePaths = "fareRule")
    List<Fare> findAllByFlightInstanceIdAndCabinAndIsActiveTrue(Long flightInstanceId, CabinClass cabin);

    // Matches any active state; the service decides how to react to inactive duplicates.
    @EntityGraph(attributePaths = "fareRule")
    Optional<Fare> findByFlightInstanceIdAndCabinAndFareClass(Long flightInstanceId, CabinClass cabin, String fareClass);

    @EntityGraph(attributePaths = "fareRule")
    List<Fare> findAllByFlightInstanceIdInAndCabinAndIsActiveTrue(List<Long> flightInstanceIds, CabinClass cabin);
}
