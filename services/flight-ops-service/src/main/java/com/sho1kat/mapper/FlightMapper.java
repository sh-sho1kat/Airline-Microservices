package com.sho1kat.mapper;

import com.sho1kat.entity.Flight;
import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.flightServiceDto.request.FlightRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightResponse;
import com.sho1kat.payload.response.AirportResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class FlightMapper {

    public Flight toEntity(FlightRequest request, Long airlineId) {
        Flight flight = new Flight();

        flight.setAirlineId(airlineId);
        flight.setIsActive(true);

        updateEntity(request, flight);

        return flight;
    }

    public void updateEntity(FlightRequest request, Flight existing) {
        if (request == null || existing == null) {
            return;
        }

        if (request.getFlightNumber() != null) {
            existing.setFlightNumber(
                    request.getFlightNumber().trim().toUpperCase(Locale.ROOT)
            );
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
                .isActive(flight.getIsActive())
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