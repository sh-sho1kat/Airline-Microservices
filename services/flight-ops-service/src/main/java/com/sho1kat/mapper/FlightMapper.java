package com.sho1kat.mapper;

import com.sho1kat.entity.Flight;
import com.sho1kat.enums.FlightStatus;
import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.flightServiceDto.request.FlightRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightResponse;
import com.sho1kat.payload.response.AirportResponse;
import org.springframework.stereotype.Component;

@Component
public class FlightMapper {

    public Flight toEntity(FlightRequest request) {
        Flight flight = new Flight();

        updateEntity(request, flight);

        if (flight.getFlightStatus() == null) {
            flight.setFlightStatus(FlightStatus.SCHEDULED);
        }

        return flight;
    }

    public void updateEntity(FlightRequest request, Flight existing) {
        if (request == null || existing == null) {
            return;
        }

        if (request.getFlightNumber() != null) {
            existing.setFlightNumber(
                    request.getFlightNumber().trim()
            );
        }

        if (request.getAirlineId() != null) {
            existing.setAirlineId(request.getAirlineId());
        }

        if (request.getAircraftId() != null) {
            existing.setAircraftId(request.getAircraftId());
        }

        if (request.getDepartureAirportId() != null) {
            existing.setDepartureAirportId(
                    request.getDepartureAirportId()
            );
        }

        if (request.getArrivalAirportId() != null) {
            existing.setArrivalAirportId(
                    request.getArrivalAirportId()
            );
        }

        if (request.getFlightStatus() != null) {
            existing.setFlightStatus(request.getFlightStatus());
        }
    }

    public FlightResponse toResponse(Flight flight) {
        if (flight == null) {
            return null;
        }

        return FlightResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .airline(toResponseAirline(flight.getAirlineId()))
                .aircraft(toResponseAircraft(flight.getAircraftId()))
                .departureAirport(
                        toResponseAirport(flight.getDepartureAirportId())
                )
                .arrivalAirport(
                        toResponseAirport(flight.getArrivalAirportId())
                )
                .status(flight.getFlightStatus())
                .createdAt(flight.getCreatedAt())
                .updatedAt(flight.getUpdatedAt())
                .build();
    }


    // Temporary. Service to service communication is not implemented yet.
    private AirlineResponse toResponseAirline(Long airlineId) {
        if (airlineId == null) {
            return null;
        }

        return AirlineResponse.builder()
                .id(airlineId)
                .build();
    }

    private AircraftResponse toResponseAircraft(Long aircraftId) {
        if (aircraftId == null) {
            return null;
        }

        return AircraftResponse.builder()
                .id(aircraftId)
                .build();
    }

    private AirportResponse toResponseAirport(Long airportId) {
        if (airportId == null) {
            return null;
        }

        return AirportResponse.builder()
                .id(airportId)
                .build();
    }
}