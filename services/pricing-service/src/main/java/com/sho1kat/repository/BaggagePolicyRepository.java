package com.sho1kat.repository;

import com.sho1kat.entity.BaggagePolicy;
import com.sho1kat.enums.CabinClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BaggagePolicyRepository extends JpaRepository<BaggagePolicy, Long> {

    // Matches any active state; the service decides how to react to inactive duplicates.
    Optional<BaggagePolicy> findByAirlineIdAndCabin(Long airlineId, CabinClass cabin);

    Optional<BaggagePolicy> findByAirlineIdAndCabinAndIsActiveTrue(Long airlineId, CabinClass cabin);

    List<BaggagePolicy> findAllByAirlineId(Long airlineId);
}
