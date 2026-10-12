package com.sho1kat.payload.airlineServiceDto.response;

import com.sho1kat.enums.AircraftStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AircraftResponse {
    private Long id;

    private String code;
    private String model;
    private String manufacturer;

    private Integer seatingCapacity;
    private Integer economySeats;
    private Integer premiumEconomySeats;
    private Integer businessSeats;
    private Integer firstClassSeats;
    private Integer totalSeats;

    private Integer rangeKm;
    private Integer cruisingSpeedKmh;
    private Integer maxAltitudeFt;

    private Integer yearOfManufacture;
    private LocalDate registrationDate;
    private LocalDate nextMaintenanceDate;

    private AircraftStatus status;
    private Boolean isAvailable;
    private Boolean operational;
    private Boolean requiresMaintenance;

    private Long airlineId;
    private String airlineName;
    private String airlineIataCode;

    private Instant createdAt;
    private Instant updatedAt;
    private Long updatedById;
}
