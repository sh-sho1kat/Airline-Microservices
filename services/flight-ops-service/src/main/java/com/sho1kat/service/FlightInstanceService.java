package com.sho1kat.service;

import com.sho1kat.enums.FlightStatus;
import com.sho1kat.payload.flightServiceDto.request.FlightInstanceRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightInstanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface FlightInstanceService {

    FlightInstanceResponse createFlightInstance(Long airlineId, FlightInstanceRequest request);

    FlightInstanceResponse getFlightInstanceById(Long instanceId);

    Page<FlightInstanceResponse> getAllFlightInstances(
            Pageable pageable
    );

    FlightInstanceResponse updateFlightInstance(
            Long airlineId,
            Long instanceId,
            FlightInstanceRequest request
    );

    FlightInstanceResponse updateFlightInstanceStatus(
            Long airlineId,
            Long instanceId,
            FlightStatus status
    );

    // Activate or deactivate flight instance
    FlightInstanceResponse updateFlightInstanceActiveStatus(
            Long airlineId,
            Long instanceId,
            Boolean isActive);

    void deleteFlightInstance(Long airlineId, Long instanceId);

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