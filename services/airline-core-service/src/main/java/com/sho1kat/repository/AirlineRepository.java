package com.sho1kat.repository;

import com.sho1kat.entity.Airline;
import com.sho1kat.enums.AirlineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
    boolean existsByIataCodeIgnoreCase(String iataCode);
    boolean existsByIcaoCodeIgnoreCase(String icaoCode);
    boolean existsByOwnerId(Long ownerId);
    Optional<Airline> findByIataCodeIgnoreCase(String iataCode);
    Optional<Airline> findFirstByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Page<Airline> findByStatusOrderByNameAsc(AirlineStatus status, Pageable pageable);
    List<Airline> findByStatus(AirlineStatus status);
}
