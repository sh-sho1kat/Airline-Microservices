package com.sho1kat.repository;

import com.sho1kat.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByIataCodeIgnoreCase(String iataCode);

    boolean existsByIataCodeIgnoreCase(String iataCode);

    boolean existsByIataCodeIgnoreCaseAndIdNot(
            String iataCode,
            Long id
    );

    List<Airport> findAllByCityId(Long cityId);
}
