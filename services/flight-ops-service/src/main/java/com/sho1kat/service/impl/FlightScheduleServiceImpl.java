package com.sho1kat.service.impl;

import com.sho1kat.entity.Flight;
import com.sho1kat.entity.FlightSchedule;
import com.sho1kat.enums.FlightStatus;
import com.sho1kat.mapper.FlightScheduleMapper;
import com.sho1kat.payload.flightServiceDto.request.FlightInstanceRequest;
import com.sho1kat.payload.flightServiceDto.request.FlightScheduleRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightScheduleResponse;
import com.sho1kat.payload.response.AirportResponse;
import com.sho1kat.repository.FlightInstanceRepository;
import com.sho1kat.repository.FlightRepository;
import com.sho1kat.repository.FlightScheduleRepository;
import com.sho1kat.service.FlightInstanceService;
import com.sho1kat.service.FlightScheduleService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightScheduleServiceImpl implements FlightScheduleService {

    private final FlightScheduleRepository scheduleRepository;
    private final FlightRepository flightRepository;
    private final FlightInstanceRepository instanceRepository;
    private final FlightScheduleMapper scheduleMapper;
    private final FlightInstanceService flightInstanceService;

    @Override
    @Transactional
    public FlightScheduleResponse createFlightSchedule(
            Long airlineId,
            FlightScheduleRequest request) {

        validateId(airlineId, "Airline ID");
        validateRequest(request);

        Flight flight = findFlight(request.getFlightId());
        validateAirlineOwnership(airlineId, flight);

        FlightSchedule schedule = scheduleMapper.toEntity(
                request,
                flight
        );

        FlightSchedule saved = scheduleRepository.save(schedule);

        if (Boolean.TRUE.equals(saved.getIsActive())) {
            generateFlightInstances(saved, flight, airlineId);
        }

        return convertToResponse(saved);
    }

    @Override
    public FlightScheduleResponse getFlightScheduleById(
            Long airlineId,
            Long scheduleId) {

        return convertToResponse(findSchedule(airlineId, scheduleId));
    }

    @Override
    public List<FlightScheduleResponse> getFlightScheduleByAirline(
            Long airlineId) {

        validateId(airlineId, "Airline ID");

        return scheduleRepository.findByFlight_AirlineId(airlineId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional
    public FlightScheduleResponse updateFlightSchedule(
            Long airlineId,
            Long scheduleId,
            FlightScheduleRequest request) {

        validateId(airlineId, "Airline ID");
        validateId(scheduleId, "Schedule ID");
        validateRequest(request);

        FlightSchedule existing = findSchedule(airlineId, scheduleId);
        Flight flight = findFlight(request.getFlightId());

        validateAirlineOwnership(airlineId, flight);

        scheduleMapper.updateEntity(request, existing, flight);

        FlightSchedule updated = scheduleRepository.save(existing);

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFlightSchedule(Long airlineId, Long scheduleId) {

        FlightSchedule schedule = findSchedule(airlineId, scheduleId);

        if (instanceRepository.existsByScheduleId(scheduleId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This schedule has generated flight instances. "
                            + "Deactivate it instead of deleting it."
            );
        }

        scheduleRepository.delete(schedule);
    }

    private void generateFlightInstances(
            FlightSchedule schedule,
            Flight flight,
            Long airlineId) {

        List<DayOfWeek> operatingDays = schedule.getOperatingDays();

        if (operatingDays == null || operatingDays.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one operating day is required"
            );
        }

        for (LocalDate date = schedule.getStartDate();
             !date.isAfter(schedule.getEndDate());
             date = date.plusDays(1)) {

            if (!operatingDays.contains(date.getDayOfWeek())) {
                continue;
            }

            LocalDate arrivalDate = date;

            if (!schedule.getArrivalTime()
                    .isAfter(schedule.getDepartureTime())) {
                arrivalDate = date.plusDays(1);
            }

            LocalDateTime departureDateTime = LocalDateTime.of(
                    date,
                    schedule.getDepartureTime()
            );

            LocalDateTime arrivalDateTime = LocalDateTime.of(
                    arrivalDate,
                    schedule.getArrivalTime()
            );

            if (instanceRepository
                    .existsByScheduleIdAndDepartureDateTime(
                            schedule.getId(),
                            departureDateTime)) {
                continue;
            }

            FlightInstanceRequest instanceRequest =
                    new FlightInstanceRequest();

            instanceRequest.setFlightId(flight.getId());
            instanceRequest.setScheduleId(schedule.getId());
            instanceRequest.setDepartureDateTime(departureDateTime);
            instanceRequest.setArrivalDateTime(arrivalDateTime);
            instanceRequest.setIsActive(true);

            flightInstanceService.createFlightInstance(
                    airlineId,
                    instanceRequest
            );
        }
    }

    private FlightScheduleResponse convertToResponse(
            FlightSchedule schedule) {

        AirportResponse departure = AirportResponse.builder()
                .id(schedule.getDepartureAirportId())
                .build();

        AirportResponse arrival = AirportResponse.builder()
                .id(schedule.getArrivalAirportId())
                .build();

        return scheduleMapper.toResponse(
                schedule,
                departure,
                arrival
        );
    }

    private Flight findFlight(Long flightId) {
        validateId(flightId, "Flight ID");

        return flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flight not found with ID: " + flightId
                ));
    }

    private FlightSchedule findSchedule(
            Long airlineId,
            Long scheduleId) {

        validateId(airlineId, "Airline ID");
        validateId(scheduleId, "Schedule ID");

        return scheduleRepository
                .findByIdAndFlight_AirlineId(scheduleId, airlineId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule not found for this airline"
                ));
    }

    private void validateAirlineOwnership(
            Long airlineId,
            Flight flight) {

        if (flight.getAirlineId() == null
                || !airlineId.equals(flight.getAirlineId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This flight does not belong to the specified airline"
            );
        }
    }

    private void validateRequest(FlightScheduleRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight schedule request is required"
            );
        }

        validateId(request.getFlightId(), "Flight ID");

        if (request.getDepartureTime() == null
                || request.getArrivalTime() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure and arrival times are required"
            );
        }

        if (request.getStartDate() == null
                || request.getEndDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date and end date are required"
            );
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date"
            );
        }

        if (request.getOperatingDays() == null
                || request.getOperatingDays().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one operating day is required"
            );
        }
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be a positive number"
            );
        }
    }
}