package com.sho1kat.payload.flightServiceDto.response;

import com.sho1kat.enums.FlightStatus;
import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.response.AirportResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

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

//    private LocalDateTime departureTime;
//
//    private LocalDateTime arrivalTime;
//
//    private FlightStatus status;
//
//    private BigDecimal lowestPrice;
//
//    private Integer totalAvailableSeats;

    private Boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
