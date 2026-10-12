package com.sho1kat.repository;

import com.sho1kat.entity.Aircraft;
import com.sho1kat.enums.AircraftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    boolean existsByAirline_Id(Long airlineId);
    Page<Aircraft> findByAirline_Id(Long airlineId, Pageable pageable);
    Page<Aircraft> findByStatusOrderByCodeAsc(AircraftStatus status, Pageable pageable);
    Page<Aircraft> findByAirline_IdAndStatus(Long airlineId, AircraftStatus status, Pageable pageable);
}
