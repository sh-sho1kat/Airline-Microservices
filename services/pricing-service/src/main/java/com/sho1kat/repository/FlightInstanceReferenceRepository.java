package com.sho1kat.repository;

import com.sho1kat.entity.FlightInstanceReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightInstanceReferenceRepository
        extends JpaRepository<FlightInstanceReference, Long> {

    /*
     * Local read model of Flight Ops instances that currently have no active fare
     * covering the departure window. The departure window defaults must be bound
     * by the caller because MySQL cannot store LocalDateTime.MIN / MAX.
     */
    @Query("""
            select r from FlightInstanceReference r
            where r.isActive = true
              and (:flightId is null or r.flightId = :flightId)
              and r.departureAt >= :start
              and r.departureAt <= :end
              and not exists (
                  select f from Fare f
                  where f.flightInstanceId = r.flightInstanceId
                    and f.isActive = true
                    and f.validFrom <= :now
                    and (f.validTo is null or f.validTo >= :now)
              )
            order by r.departureAt asc
            """)
    List<FlightInstanceReference> findUnpricedInstances(
            @Param("flightId") Long flightId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
