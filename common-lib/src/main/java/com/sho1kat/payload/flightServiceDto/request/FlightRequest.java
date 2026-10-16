package com.sho1kat.payload.flightServiceDto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FlightRequest {

    @NotBlank(message = "Flight number is required")
    @Size(max = 20, message = "Flight number cannot exceed 20 characters")
    private String flightNumber;

    @NotNull(message = "Airline ID is required")
    @Positive(message = "Airline ID must be positive")
    private Long airlineId;

    @NotNull(message = "Aircraft ID is required")
    @Positive(message = "Aircraft ID must be positive")
    private Long aircraftId;

    @NotNull(message = "Departure airport ID is required")
    @Positive(message = "Departure airport ID must be positive")
    private Long departureAirportId;

    @NotNull(message = "Arrival airport ID is required")
    @Positive(message = "Arrival airport ID must be positive")
    private Long arrivalAirportId;

//    private FlightStatus flightStatus = FlightStatus.SCHEDULED;
}
