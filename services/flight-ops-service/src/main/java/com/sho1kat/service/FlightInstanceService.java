package com.sho1kat.service;

import com.sho1kat.enums.FlightStatus;
import com.sho1kat.payload.flightServiceDto.request.FlightInstanceRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightInstanceResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface FlightInstanceService {

    FlightInstanceResponse createFlightInstance(Long callerId, FlightInstanceRequest request);

    FlightInstanceResponse getFlightInstanceById(Long instanceId);

    Page<FlightInstanceResponse> getAllFlightInstances(
            Pageable pageable
    );

    FlightInstanceResponse updateFlightInstance(
            Long instanceId,
            FlightInstanceRequest request
    );

    FlightInstanceResponse updateFlightInstanceStatus(
            Long instanceId,
            FlightStatus status
    );

    // Activate or deactivate flight instance
    @Transactional
    FlightInstanceResponse updateFlightInstanceActiveStatus(
            Long instanceId,
            Boolean isActive);

    void deleteFlightInstance(Long instanceId);

    Page<FlightInstanceResponse> searchFlightInstances(
            Long flightId,
            Long airlineId,
            LocalDate departureDate,
            Long departureAirportId,
            Long arrivalAirportId,
            FlightStatus status,
            Boolean isActive,
            Pageable pageable
    );
}