package com.sho1kat.service;

import com.sho1kat.enums.FlightStatus;
import com.sho1kat.payload.flightServiceDto.request.FlightRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface FlightService {

    FlightResponse createFlight(FlightRequest request);

    FlightResponse getFlightById(Long flightId);

    Page<FlightResponse> getAllFlights(Pageable pageable);

    FlightResponse updateFlight(String flightNumber, FlightRequest request);

    FlightResponse updateFlightStatus(String flightNumber, FlightStatus status);

    void deleteFlight(Long airlineId, String flightNumber);

    Page<FlightResponse> searchFlights(
            Long departureAirportId,
            Long arrivalAirportId,
            LocalDate departureDate,
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

    Page<FlightResponse> searchFlightsByDate(
            LocalDate departureDate,
            Pageable pageable
    );

    Page<FlightResponse> searchFlightsByStatus(
            FlightStatus status,
            Pageable pageable
    );
}