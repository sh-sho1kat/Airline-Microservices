package com.sho1kat.payload.pricingServiceDto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FlightInstanceSyncRequest {

    @NotNull(message = "Flight instance ID is required")
    @Positive(message = "Flight instance ID must be positive")
    private Long flightInstanceId;

    @NotNull(message = "Flight ID is required")
    @Positive(message = "Flight ID must be positive")
    private Long flightId;

    @NotNull(message = "Airline ID is required")
    @Positive(message = "Airline ID must be positive")
    private Long airlineId;

    @NotNull(message = "Departure date and time are required")
    private LocalDateTime departureAt;

    private Boolean isActive = true;
}
