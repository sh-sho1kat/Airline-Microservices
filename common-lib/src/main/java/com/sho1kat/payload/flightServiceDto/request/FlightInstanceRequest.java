package com.sho1kat.payload.flightServiceDto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FlightInstanceRequest {

    @NotNull(message = "Flight ID is required")
    @Positive(message = "Flight ID must be positive")
    private Long flightId;

    @NotNull(message = "Schedule ID is required")
    @Positive(message = "Schedule ID must be positive")
    private Long scheduleId;

    @Positive(message = "Aircraft ID must be positive")
    private Long aircraftId;

    @NotNull(message = "Departure date and time are required")
    private LocalDateTime departureDateTime;

    @NotNull(message = "Arrival date and time are required")
    private LocalDateTime arrivalDateTime;

    @PositiveOrZero(message = "Minimum advance booking days cannot be negative")
    private Integer minAdvanceBookingDays;

    @PositiveOrZero(message = "Maximum advance booking days cannot be negative")
    private Integer maxAdvanceBookingDays;

    private Boolean isActive = true;
}