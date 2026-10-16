package com.sho1kat.payload.flightServiceDto.response;

import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.response.AirportResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightResponse {

    private Long id;

    private String flightNumber;

    private AirlineResponse airline;

    private AircraftResponse aircraft;

    private AirportResponse departureAirport;

    private AirportResponse arrivalAirport;

    private Boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
