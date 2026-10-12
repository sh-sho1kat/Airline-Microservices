package com.sho1kat.mapper;


import com.sho1kat.entity.Aircraft;
import com.sho1kat.entity.Airline;
import com.sho1kat.enums.AircraftStatus;
import com.sho1kat.payload.airlineServiceDto.request.AircraftRequest;
import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AircraftStatusResponse;

import java.util.Locale;

public final class AircraftMapper {
    private AircraftMapper() {
    }

    public static Aircraft toEntity(AircraftRequest request, Airline airline, Long callerId) {
        Aircraft aircraft = Aircraft.builder()
                .code(normalizeCode(request.getCode()))
                .model(request.getModel().trim())
                .manufacturer(request.getManufacturer().trim())
                .seatingCapacity(request.getSeatingCapacity())
                .economySeats(valueOrZero(request.getEconomySeats()))
                .premiumEconomySeats(valueOrZero(request.getPremiumEconomySeats()))
                .businessSeats(valueOrZero(request.getBusinessSeats()))
                .firstClassSeats(valueOrZero(request.getFirstClassSeats()))
                .rangeKm(request.getRangeKm())
                .cruisingSpeedKmh(request.getCruisingSpeedKmh())
                .maxAltitudeFt(request.getMaxAltitudeFt())
                .yearOfManufacture(request.getYearOfManufacture())
                .registrationDate(request.getRegistrationDate())
                .nextMaintenanceDate(request.getNextMaintenanceDate())
                .status(request.getStatus() == null ? AircraftStatus.ACTIVE : request.getStatus())
                .isAvailable(request.getIsAvailable() == null ? Boolean.TRUE : request.getIsAvailable())
                .airline(airline)
                .updatedById(callerId)
                .build();
        validateSeatCounts(aircraft);
        return aircraft;
    }

    public static void applyUpdate(Aircraft aircraft, AircraftRequest request, Airline airline, Long callerId) {
        aircraft.setCode(normalizeCode(request.getCode()));
        aircraft.setModel(request.getModel().trim());
        aircraft.setManufacturer(request.getManufacturer().trim());
        aircraft.setSeatingCapacity(request.getSeatingCapacity());
        aircraft.setEconomySeats(valueOrZero(request.getEconomySeats()));
        aircraft.setPremiumEconomySeats(valueOrZero(request.getPremiumEconomySeats()));
        aircraft.setBusinessSeats(valueOrZero(request.getBusinessSeats()));
        aircraft.setFirstClassSeats(valueOrZero(request.getFirstClassSeats()));
        aircraft.setRangeKm(request.getRangeKm());
        aircraft.setCruisingSpeedKmh(request.getCruisingSpeedKmh());
        aircraft.setMaxAltitudeFt(request.getMaxAltitudeFt());
        aircraft.setYearOfManufacture(request.getYearOfManufacture());
        aircraft.setRegistrationDate(request.getRegistrationDate());
        aircraft.setNextMaintenanceDate(request.getNextMaintenanceDate());
        if (request.getStatus() != null) aircraft.setStatus(request.getStatus());
        if (request.getIsAvailable() != null) aircraft.setIsAvailable(request.getIsAvailable());
        aircraft.setAirline(airline);
        validateSeatCounts(aircraft);
        aircraft.setUpdatedById(callerId);
    }

    public static AircraftResponse toResponse(Aircraft aircraft) {
        if (aircraft == null) return null;
        Airline airline = aircraft.getAirline();
        return AircraftResponse.builder()
                .id(aircraft.getId())
                .code(aircraft.getCode())
                .model(aircraft.getModel())
                .manufacturer(aircraft.getManufacturer())
                .seatingCapacity(aircraft.getSeatingCapacity())
                .economySeats(aircraft.getEconomySeats())
                .premiumEconomySeats(aircraft.getPremiumEconomySeats())
                .businessSeats(aircraft.getBusinessSeats())
                .firstClassSeats(aircraft.getFirstClassSeats())
                .totalSeats(aircraft.getTotalSeats())
                .rangeKm(aircraft.getRangeKm())
                .cruisingSpeedKmh(aircraft.getCruisingSpeedKmh())
                .maxAltitudeFt(aircraft.getMaxAltitudeFt())
                .yearOfManufacture(aircraft.getYearOfManufacture())
                .registrationDate(aircraft.getRegistrationDate())
                .nextMaintenanceDate(aircraft.getNextMaintenanceDate())
                .status(aircraft.getStatus())
                .isAvailable(aircraft.getIsAvailable())
                .operational(aircraft.isOperational())
                .requiresMaintenance(aircraft.requiresMaintenance())
                .airlineId(airline.getId())
                .airlineName(airline.getName())
                .airlineIataCode(airline.getIataCode())
                .createdAt(aircraft.getCreatedAt())
                .updatedAt(aircraft.getUpdatedAt())
                .updatedById(aircraft.getUpdatedById())
                .build();
    }
    public static AircraftStatusResponse toStatusResponse(Aircraft aircraft) {
        if (aircraft == null) return null;
        return AircraftStatusResponse.builder()
                .aircraftCode(aircraft.getCode())
                .airlineName(aircraft.getAirline().getName())
                .status(aircraft.getStatus())
                .isAvailable(aircraft.getIsAvailable())
                .operational(aircraft.isOperational())
                .requiresMaintenance(aircraft.requiresMaintenance())
                .nextMaintenanceDate(aircraft.getNextMaintenanceDate())
                .build();
    }

    private static void validateSeatCounts(Aircraft aircraft) {
        if (aircraft.getTotalSeats() > aircraft.getSeatingCapacity()) {
            throw new IllegalArgumentException("Cabin seat counts cannot exceed total seating capacity");
        }
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
