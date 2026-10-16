package com.sho1kat.service.impl;

import com.sho1kat.entity.Flight;
import com.sho1kat.entity.FlightInstance;
import com.sho1kat.entity.FlightSchedule;
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
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightScheduleServiceImpl implements FlightScheduleService {

    private final FlightScheduleRepository scheduleRepository;
    private final FlightRepository flightRepository;
    private final FlightInstanceRepository instanceRepository;
    private final FlightScheduleMapper scheduleMapper;
    private final FlightInstanceService flightInstanceService;

    private static final long MAX_SCHEDULE_DAYS = 366;

    @Override
    @Transactional
    public FlightScheduleResponse createFlightSchedule(
            Long airlineId,
            FlightScheduleRequest request) {

        validateId(airlineId, "Airline ID");
        validateRequest(request);

        if (request.getEndDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be in the past"
            );
        }

        Flight flight = findFlight(request.getFlightId());
        validateAirlineOwnership(airlineId, flight);
        validateFlightIsActive(flight);

        FlightSchedule saved = scheduleRepository.save(
                scheduleMapper.toEntity(request, flight)
        );

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

        if (!existing.getFlight().getId().equals(request.getFlightId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The flight of a schedule cannot be changed. Create a new schedule."
            );
        }

        if (instanceRepository.existsBySchedule_Id(scheduleId)) {
            if (patternChanged(existing, request)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Times, start date and operating days cannot be changed after "
                                + "instances were generated. Extend the end date, "
                                + "deactivate this schedule, or create a new one."
                );
            }

            if (request.getEndDate().isBefore(existing.getEndDate())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "The end date cannot be shortened after instances were generated"
                );
            }
        }

        boolean wasActive = Boolean.TRUE.equals(existing.getIsActive());

        scheduleMapper.updateEntity(request, existing);

        FlightSchedule updated = scheduleRepository.save(existing);
        boolean isActive = Boolean.TRUE.equals(updated.getIsActive());

        if (isActive) {
            validateFlightIsActive(updated.getFlight());
            generateFlightInstances(updated, updated.getFlight(), airlineId);

            if (!wasActive) {
                setFutureUnbookedInstancesActive(updated.getId(), true);
            }
        } else if (wasActive) {
            setFutureUnbookedInstancesActive(updated.getId(), false);
        }

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFlightSchedule(Long airlineId, Long scheduleId) {

        FlightSchedule schedule = findSchedule(airlineId, scheduleId);

        if (instanceRepository.existsBySchedule_Id(scheduleId)) {
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

        Set<DayOfWeek> operatingDays = schedule.getOperatingDays();

        if (operatingDays == null || operatingDays.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one operating day is required"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        for (LocalDate date = schedule.getStartDate();
             !date.isAfter(schedule.getEndDate());
             date = date.plusDays(1)) {

            if (!operatingDays.contains(date.getDayOfWeek())) {
                continue;
            }

            LocalDateTime departureDateTime = LocalDateTime.of(
                    date,
                    schedule.getDepartureTime()
            );

            // Never create departures in the past
            if (!departureDateTime.isAfter(now)) {
                continue;
            }

            // Arrival earlier than departure means the flight lands the next day
            LocalDate arrivalDate = schedule.getArrivalTime()
                    .isAfter(schedule.getDepartureTime())
                    ? date
                    : date.plusDays(1);

            LocalDateTime arrivalDateTime = LocalDateTime.of(
                    arrivalDate,
                    schedule.getArrivalTime()
            );

            // One departure per flight and time, even across different schedules
            if (instanceRepository.existsByFlight_IdAndDepartureDateTime(
                    flight.getId(),
                    departureDateTime)) {
                continue;
            }

            FlightInstanceRequest instanceRequest = new FlightInstanceRequest();

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

        Flight flight = schedule.getFlight();

        AirportResponse departure = AirportResponse.builder()
                .id(flight.getDepartureAirportId())
                .build();

        AirportResponse arrival = AirportResponse.builder()
                .id(flight.getArrivalAirportId())
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

        if (request.getDepartureTime().equals(request.getArrivalTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure and arrival times cannot be the same"
            );
        }

        if (ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1
                > MAX_SCHEDULE_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A schedule can cover at most " + MAX_SCHEDULE_DAYS
                            + " days. Create several schedules for longer periods."
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

    private void validateFlightIsActive(Flight flight) {
        if (!Boolean.TRUE.equals(flight.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Flight is not active"
            );
        }
    }

    private boolean patternChanged(
            FlightSchedule existing,
            FlightScheduleRequest request) {

        return !existing.getDepartureTime().equals(request.getDepartureTime())
                || !existing.getArrivalTime().equals(request.getArrivalTime())
                || !existing.getStartDate().equals(request.getStartDate())
                || !new HashSet<>(existing.getOperatingDays())
                .equals(new HashSet<>(request.getOperatingDays()));
    }

    // Only instances without booked seats are touched.
    private void setFutureUnbookedInstancesActive(Long scheduleId, boolean active) {
        List<FlightInstance> future =
                instanceRepository.findBySchedule_IdAndDepartureDateTimeAfter(
                        scheduleId,
                        LocalDateTime.now()
                );

        for (FlightInstance instance : future) {
            boolean hasBookings =
                    instance.getTotalSeats() - instance.getAvailableSeats() > 0;

            if (!hasBookings) {
                instance.setIsActive(active);
            }
        }

        instanceRepository.saveAll(future);
    }
}