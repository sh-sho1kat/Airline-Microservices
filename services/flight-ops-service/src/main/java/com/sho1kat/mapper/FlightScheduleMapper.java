package com.sho1kat.mapper;

import com.sho1kat.entity.Flight;
import com.sho1kat.entity.FlightSchedule;
import com.sho1kat.payload.flightServiceDto.request.FlightScheduleRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightScheduleResponse;
import com.sho1kat.payload.response.AirportResponse;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class FlightScheduleMapper {

    public FlightSchedule toEntity(
            FlightScheduleRequest request,
            Flight flight) {

        if (request == null || flight == null) {
            return null;
        }

        FlightSchedule schedule = new FlightSchedule();

        schedule.setFlight(flight);

        schedule.setDepartureTime(request.getDepartureTime());
        schedule.setArrivalTime(request.getArrivalTime());
        schedule.setStartDate(request.getStartDate());
        schedule.setEndDate(request.getEndDate());
        schedule.setOperatingDays(new HashSet<>(request.getOperatingDays()));

        schedule.setIsActive(
                request.getIsActive() != null
                        ? request.getIsActive()
                        : true
        );

        return schedule;
    }

    public void updateEntity(
            FlightScheduleRequest request,
            FlightSchedule existing) {

        if (request == null || existing == null) {
            return;
        }

        if (request.getDepartureTime() != null) {
            existing.setDepartureTime(request.getDepartureTime());
        }

        if (request.getArrivalTime() != null) {
            existing.setArrivalTime(request.getArrivalTime());
        }

        if (request.getStartDate() != null) {
            existing.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            existing.setEndDate(request.getEndDate());
        }

        if (request.getOperatingDays() != null) {
            existing.getOperatingDays().clear();
            existing.getOperatingDays().addAll(request.getOperatingDays());
        }

        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
    }

    public FlightScheduleResponse toResponse(
            FlightSchedule schedule,
            AirportResponse departureAirport,
            AirportResponse arrivalAirport) {

        if (schedule == null) {
            return null;
        }

        Flight flight = schedule.getFlight();

        return FlightScheduleResponse.builder()
                .id(schedule.getId())
                .flightId(flight != null ? flight.getId() : null)
                .flightNumber(
                        flight != null ? flight.getFlightNumber() : null
                )
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .departureTime(schedule.getDepartureTime())
                .arrivalTime(schedule.getArrivalTime())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .operatingDays(
                        schedule.getOperatingDays() == null
                                ? java.util.List.of()
                                : schedule.getOperatingDays().stream().sorted().toList()
                )
                .isActive(schedule.getIsActive())
                .build();
    }
}