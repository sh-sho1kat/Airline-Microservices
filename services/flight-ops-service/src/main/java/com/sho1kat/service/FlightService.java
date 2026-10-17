package com.sho1kat.service;

import com.sho1kat.payload.flightServiceDto.request.FlightRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightService {

    FlightResponse createFlight(Long airlineId, FlightRequest request);

    FlightResponse getFlightById(Long flightId);

    Page<FlightResponse> getAllFlights(Pageable pageable);

    FlightResponse updateFlight(Long airlineId, String flightNumber, FlightRequest request);

    FlightResponse updateFlightActiveStatus(Long airlineId, String flightNumber, Boolean isActive);

    void deleteFlight(Long airlineId, String flightNumber);

    Page<FlightResponse> searchFlights(
            Long departureAirportId,
            Long arrivalAirportId,
            Pageable pageable
    );

    Page<FlightResponse> searchFlightsByAirline(
            Long airlineId,
            Long departureAirportId,
            Long arrivalAirportId,
            Pageable pageable
    );

    Page<FlightResponse> searchFlightsByAircraft(
            Long aircraftId,
            Long departureAirportId,
            Long arrivalAirportId,
            Pageable pageable
    );
}