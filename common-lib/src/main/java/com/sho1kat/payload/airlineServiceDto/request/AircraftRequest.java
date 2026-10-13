package com.sho1kat.payload.airlineServiceDto.request;

import com.sho1kat.enums.AircraftStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AircraftRequest {
    @NotBlank(message = "Aircraft code is required")
    @Size(max = 20, message = "Aircraft code must not exceed 20 characters")
    private String code;

    @NotBlank(message = "Aircraft model is required")
    @Size(max = 100, message = "Aircraft model must not exceed 100 characters")
    private String model;

    @NotBlank(message = "Manufacturer is required")
    @Size(max = 50, message = "Manufacturer must not exceed 50 characters")
    private String manufacturer;

    @NotNull(message = "Airline ID is required")
    @Positive(message = "Airline ID must be positive")
    private Long airlineId;

    @NotNull(message = "Seating capacity is required")
    @Positive(message = "Seating capacity must be positive")
    private Integer seatingCapacity;

    @PositiveOrZero(message = "Economy seats cannot be negative")
    @Builder.Default
    private Integer economySeats = 0;

    @PositiveOrZero(message = "Premium economy seats cannot be negative")
    private Integer premiumEconomySeats = 0;

    @PositiveOrZero(message = "Business seats cannot be negative")
    private Integer businessSeats = 0;

    @PositiveOrZero(message = "First class seats cannot be negative")
    private Integer firstClassSeats = 0;

    @PositiveOrZero(message = "Range must be zero or greater")
    private Integer rangeKm;

    @PositiveOrZero(message = "Cruising speed must be zero or greater")
    private Integer cruisingSpeedKmh;

    @PositiveOrZero(message = "Maximum altitude must be zero or greater")
    private Integer maxAltitudeFt;

    @Min(value = 1950, message = "yearOfManufacture must be 1950 or later")
    @Max(value = 2100, message = "yearOfManufacture must be 2100 or earlier")
    private Integer yearOfManufacture;

    private LocalDate registrationDate;
    private LocalDate nextMaintenanceDate;
    private AircraftStatus status;
    private Boolean isAvailable;

    @AssertTrue(message = "Cabin seat counts cannot exceed total seating capacity")
    public boolean isCabinSeatCountValid() {
        if (seatingCapacity == null) return true;
        return safe(economySeats) + safe(premiumEconomySeats)
                + safe(businessSeats) + safe(firstClassSeats) <= seatingCapacity;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}