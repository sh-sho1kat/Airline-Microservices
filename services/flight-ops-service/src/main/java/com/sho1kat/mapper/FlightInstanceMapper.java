package com.sho1kat.mapper;

import com.sho1kat.entity.Flight;
import com.sho1kat.entity.FlightInstance;
import com.sho1kat.entity.FlightSchedule;
import com.sho1kat.enums.FlightStatus;

import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.flightServiceDto.request.FlightInstanceRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightInstanceResponse;
import com.sho1kat.payload.response.AirportResponse;

import org.springframework.stereotype.Component;

@Component
public class FlightInstanceMapper {

    public FlightInstance toEntity(
            FlightInstanceRequest request,
            Flight flight,
            FlightSchedule schedule,
            Long aircraftId) {

        FlightInstance instance = new FlightInstance();

        instance.setFlight(flight);
        instance.setSchedule(schedule);
        instance.setAircraftId(aircraftId);
        instance.setAirlineId(flight.getAirlineId());
        instance.setDepartureAirportId(flight.getDepartureAirportId());
        instance.setArrivalAirportId(flight.getArrivalAirportId());
        instance.setDepartureDateTime(request.getDepartureDateTime());
        instance.setArrivalDateTime(request.getArrivalDateTime());
        instance.setStatus(FlightStatus.SCHEDULED);
        instance.setMinAdvanceBookingDays(request.getMinAdvanceBookingDays());
        instance.setMaxAdvanceBookingDays(request.getMaxAdvanceBookingDays());
        instance.setIsActive(request.getIsActive() == null || request.getIsActive());

        return instance;
    }

    public void updateEntity(
            FlightInstanceRequest request,
            FlightInstance existing) {

        if (request == null || existing == null) {
            return;
        }

        if (request.getDepartureDateTime() != null) {
            existing.setDepartureDateTime(request.getDepartureDateTime());
        }

        if (request.getArrivalDateTime() != null) {
            existing.setArrivalDateTime(request.getArrivalDateTime());
        }

        if (request.getMinAdvanceBookingDays() != null) {
            existing.setMinAdvanceBookingDays(request.getMinAdvanceBookingDays());
        }

        if (request.getMaxAdvanceBookingDays() != null) {
            existing.setMaxAdvanceBookingDays(request.getMaxAdvanceBookingDays());
        }

        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        // Flight, schedule and aircraft are changed by the service, never by the mapper.
        // Seat inventory belongs to booking operations.
    }

    public FlightInstanceResponse toResponse(
            FlightInstance fi,
            AircraftResponse aircraft,
            AirlineResponse airline,
            AirportResponse departureAirport,
            AirportResponse arrivalAirport) {

        if (fi == null) {
            return null;
        }

        Flight flight = fi.getFlight();

        return FlightInstanceResponse.builder()
                .id(fi.getId())
                .flightId(flight != null ? flight.getId() : null)
                .scheduleId(fi.getSchedule() != null ? fi.getSchedule().getId() : null)
                .flightNumber(
                        flight != null ? flight.getFlightNumber() : null
                )
                .airlineId(fi.getAirlineId())
                .airlineName(
                        airline != null ? airline.getName() : null
                )
                .airlineLogo(
                        airline != null ? airline.getLogoUrl() : null
                )
                .aircraftId(fi.getAircraftId())
                .aircraftModel(
                        aircraft != null ? aircraft.getModel() : null
                )
                .aircraftCode(
                        aircraft != null ? aircraft.getCode() : null
                )
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .departureDateTime(fi.getDepartureDateTime())
                .arrivalDateTime(fi.getArrivalDateTime())
                .formattedDuration(fi.getFormattedDuration())
                .totalSeats(fi.getTotalSeats())
                .availableSeats(fi.getAvailableSeats())
                .status(fi.getStatus())
                .minAdvanceBookingDays(
                        fi.getMinAdvanceBookingDays()
                )
                .maxAdvanceBookingDays(
                        fi.getMaxAdvanceBookingDays()
                )
                .isActive(fi.getIsActive())
                .build();
    }
}