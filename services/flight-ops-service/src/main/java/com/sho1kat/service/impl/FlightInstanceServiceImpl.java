
package com.sho1kat.service.impl;

import com.sho1kat.entity.Flight;
import com.sho1kat.entity.FlightInstance;
import com.sho1kat.entity.FlightSchedule;
import com.sho1kat.enums.FlightStatus;
import com.sho1kat.mapper.FlightInstanceMapper;
import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.flightServiceDto.request.FlightInstanceRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightInstanceResponse;
import com.sho1kat.payload.response.AirportResponse;
import com.sho1kat.repository.FlightInstanceRepository;
import com.sho1kat.repository.FlightRepository;
import com.sho1kat.repository.FlightScheduleRepository;
import com.sho1kat.service.FlightInstanceService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightInstanceServiceImpl implements FlightInstanceService {

    private final FlightInstanceRepository flightInstanceRepository;
    private final FlightRepository flightRepository;
    private final FlightScheduleRepository flightScheduleRepository;
    private final FlightInstanceMapper flightInstanceMapper;

    /*
     * TEMPORARY: replace with the airline-service client.
     * Aircraft capacity must come from the authoritative aircraft record.
     */
    private static final int TEMPORARY_AIRCRAFT_CAPACITY = 90;

    // Excludes nothing in the aircraft-conflict query (instance not stored yet)
    private static final Long NO_INSTANCE_ID = -1L;

    // Once a flight has left the gate or ended, its times, aircraft and active flag are frozen.
    private static final Set<FlightStatus> LOCKED_STATUSES = EnumSet.of(
            FlightStatus.DEPARTED,
            FlightStatus.IN_AIR,
            FlightStatus.LANDED,
            FlightStatus.ARRIVED,
            FlightStatus.DIVERTED,
            FlightStatus.COMPLETED,
            FlightStatus.CANCELLED
    );

    private static final Map<FlightStatus, Set<FlightStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(FlightStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(FlightStatus.SCHEDULED,
                EnumSet.of(FlightStatus.DELAYED, FlightStatus.BOARDING, FlightStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(FlightStatus.DELAYED,
                EnumSet.of(FlightStatus.SCHEDULED, FlightStatus.BOARDING, FlightStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(FlightStatus.BOARDING,
                EnumSet.of(FlightStatus.DEPARTED, FlightStatus.DELAYED, FlightStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(FlightStatus.DEPARTED,
                EnumSet.of(FlightStatus.IN_AIR, FlightStatus.LANDED, FlightStatus.DIVERTED));
        ALLOWED_TRANSITIONS.put(FlightStatus.IN_AIR,
                EnumSet.of(FlightStatus.LANDED, FlightStatus.DIVERTED));
        ALLOWED_TRANSITIONS.put(FlightStatus.LANDED,
                EnumSet.of(FlightStatus.ARRIVED, FlightStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(FlightStatus.ARRIVED,
                EnumSet.of(FlightStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(FlightStatus.DIVERTED,
                EnumSet.of(FlightStatus.LANDED, FlightStatus.ARRIVED));
        ALLOWED_TRANSITIONS.put(FlightStatus.COMPLETED, EnumSet.noneOf(FlightStatus.class));
        ALLOWED_TRANSITIONS.put(FlightStatus.CANCELLED, EnumSet.noneOf(FlightStatus.class));
    }



    @Override
    @Transactional
    public FlightInstanceResponse createFlightInstance(Long airlineId, FlightInstanceRequest request) {

        validateId(airlineId, "Airline ID");
        validateRequest(request);

        Flight flight = findFlight(request.getFlightId());
        validateAirlineOwnership(airlineId, flight);

        if (!Boolean.TRUE.equals(flight.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Flight is not active");
        }

        FlightSchedule schedule = findSchedule(request.getScheduleId());

        if (!schedule.getFlight().getId().equals(flight.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The schedule does not belong to this flight"
            );
        }

        validateRoute(flight.getDepartureAirportId(), flight.getArrivalAirportId());
        validateBookingWindow(request.getMinAdvanceBookingDays(), request.getMaxAdvanceBookingDays());

        LocalDateTime departure = request.getDepartureDateTime();
        LocalDateTime arrival = request.getArrivalDateTime();

        if (!departure.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure must be in the future"
            );
        }

        if (flightInstanceRepository.existsByFlight_IdAndDepartureDateTime(flight.getId(), departure)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This flight already has an instance departing at " + departure
            );
        }

        Long aircraftId = request.getAircraftId() != null
                ? request.getAircraftId()
                : flight.getAircraftId();

        validateAircraftAvailable(aircraftId, departure, arrival, NO_INSTANCE_ID);

        // TODO: Fetch AircraftResponse using your service-to-service client
        // and also verify that the aircraft is operational.
        Integer capacity = getAircraft(aircraftId).getTotalSeats();

        if (capacity == null || capacity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Aircraft capacity is unavailable"
            );
        }

        FlightInstance instance =
                flightInstanceMapper.toEntity(request, flight, schedule, aircraftId);

        instance.setTotalSeats(capacity);
        instance.setAvailableSeats(capacity);

        return convertToFlightInstanceResponse(flightInstanceRepository.save(instance));
    }

    // Get flight instance by ID
    @Override
    public FlightInstanceResponse getFlightInstanceById(
            Long instanceId) {

        return convertToFlightInstanceResponse(
                findFlightInstance(instanceId)
        );
    }

    // Get all flight instances
    @Override
    public Page<FlightInstanceResponse> getAllFlightInstances(
            Pageable pageable) {

        return flightInstanceRepository.findAll(pageable)
                .map(this::convertToFlightInstanceResponse);
    }

    // Update flight instance
    @Override
    @Transactional
    public FlightInstanceResponse updateFlightInstance(
            Long airlineId,
            Long instanceId,
            FlightInstanceRequest request) {

        validateId(airlineId, "Airline ID");
        validateRequest(request);

        FlightInstance existing = findOwnedInstance(airlineId, instanceId);
        requireModifiable(existing);

        if (!existing.getFlight().getId().equals(request.getFlightId())
                || !existing.getSchedule().getId().equals(request.getScheduleId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The flight and schedule of an instance cannot be changed"
            );
        }

        validateBookingWindow(request.getMinAdvanceBookingDays(), request.getMaxAdvanceBookingDays());

        LocalDateTime departure = request.getDepartureDateTime();
        LocalDateTime arrival = request.getArrivalDateTime();

        boolean departureChanged = !departure.equals(existing.getDepartureDateTime());
        boolean timeChanged = departureChanged || !arrival.equals(existing.getArrivalDateTime());

        if (departureChanged && flightInstanceRepository.existsByFlight_IdAndDepartureDateTime(
                existing.getFlight().getId(), departure)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This flight already has an instance departing at " + departure
            );
        }

        Long aircraftId = request.getAircraftId() != null
                ? request.getAircraftId()
                : existing.getAircraftId();
        boolean aircraftChanged = !aircraftId.equals(existing.getAircraftId());

        if (timeChanged || aircraftChanged) {
            validateAircraftAvailable(aircraftId, departure, arrival, existing.getId());
        }

        if (aircraftChanged) {
            int bookedSeats = existing.getTotalSeats() - existing.getAvailableSeats();

            if (bookedSeats < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid seat inventory");
            }

            Integer capacity = getAircraft(aircraftId).getTotalSeats();

            if (capacity == null || capacity <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Aircraft capacity is unavailable"
                );
            }

            if (capacity < bookedSeats) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Aircraft capacity cannot be less than already-booked seats"
                );
            }

            existing.setAircraftId(aircraftId);
            existing.setTotalSeats(capacity);
            existing.setAvailableSeats(capacity - bookedSeats);
        }

        flightInstanceMapper.updateEntity(request, existing);

        return convertToFlightInstanceResponse(flightInstanceRepository.save(existing));
    }

    // Update flight instance status
    @Override
    @Transactional
    public FlightInstanceResponse updateFlightInstanceStatus(
            Long airlineId,
            Long instanceId,
            FlightStatus status) {

        validateId(airlineId, "Airline ID");

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight status is required"
            );
        }

        FlightInstance instance = findOwnedInstance(airlineId, instanceId);

        if (instance.getStatus() != status) {
            Set<FlightStatus> allowed =
                    ALLOWED_TRANSITIONS.getOrDefault(instance.getStatus(), Set.of());

            if (!allowed.contains(status)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot change flight status from " + instance.getStatus()
                                + " to " + status
                );
            }

            instance.setStatus(status);

            if (status == FlightStatus.CANCELLED) {
                instance.setIsActive(false);
                // TODO: notify booking-service so booked passengers are refunded or re-accommodated
            }
        }

        return convertToFlightInstanceResponse(flightInstanceRepository.save(instance));
    }

    // Activate or deactivate flight instance
    @Transactional
    @Override
    public FlightInstanceResponse updateFlightInstanceActiveStatus(
            Long airlineId,
            Long instanceId,
            Boolean isActive) {

        validateId(airlineId, "Airline ID");

        if (isActive == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Active status is required"
            );
        }

        FlightInstance instance = findOwnedInstance(airlineId, instanceId);
        requireModifiable(instance);

        instance.setIsActive(isActive);

        return convertToFlightInstanceResponse(
                flightInstanceRepository.save(instance)
        );
    }

    // Delete flight instance
    @Override
    @Transactional
    public void deleteFlightInstance(Long airlineId, Long instanceId) {

        validateId(airlineId, "Airline ID");

        FlightInstance instance = findOwnedInstance(airlineId, instanceId);

        int bookedSeats = instance.getTotalSeats()
                - instance.getAvailableSeats();

        if (bookedSeats > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a flight instance with booked seats"
            );
        }

        flightInstanceRepository.delete(instance);
    }

    // Search using optional filters
    @Override
    public Page<FlightInstanceResponse> searchFlightInstances(
            Long flightId,
            Long airlineId,
            LocalDate departureDate,
            Long departureAirportId,
            Long arrivalAirportId,
            FlightStatus status,
            Boolean isActive,
            Pageable pageable) {

        validateOptionalId(flightId, "Flight ID");
        validateOptionalId(airlineId, "Airline ID");
        validateOptionalId(
                departureAirportId,
                "Departure airport ID"
        );
        validateOptionalId(
                arrivalAirportId,
                "Arrival airport ID"
        );

        LocalDateTime dayStart = departureDate == null
                ? null
                : departureDate.atStartOfDay();

        LocalDateTime dayEnd = departureDate == null
                ? null
                : departureDate.plusDays(1).atStartOfDay();

        return flightInstanceRepository.searchFlightInstances(
                        airlineId,
                        departureAirportId,
                        arrivalAirportId,
                        flightId,
                        dayStart,
                        dayEnd,
                        status,
                        isActive,
                        pageable
                )
                .map(this::convertToFlightInstanceResponse);
    }

    // Convert entity to response using temporary ID-only references.
    private FlightInstanceResponse convertToFlightInstanceResponse(
            FlightInstance instance) {

        Flight flight = instance.getFlight();

        AirlineResponse airline = AirlineResponse.builder()
                .id(instance.getAirlineId())
                .build();

        AircraftResponse aircraft = AircraftResponse.builder()
                .id(instance.getAircraftId())
                .totalSeats(instance.getTotalSeats())
                .build();

        AirportResponse departureAirport = AirportResponse.builder()
                .id(instance.getDepartureAirportId())
                .build();

        AirportResponse arrivalAirport = AirportResponse.builder()
                .id(instance.getArrivalAirportId())
                .build();

        return flightInstanceMapper.toResponse(
                instance,
                aircraft,
                airline,
                departureAirport,
                arrivalAirport
        );
    }

    // Temporary aircraft lookup.
    // Replace this method with a call to Airline Core Service.
    private AircraftResponse getAircraft(Long aircraftId) {

        if (aircraftId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight instance has no assigned aircraft"
            );
        }

        return AircraftResponse.builder()
                .id(aircraftId)
                .totalSeats(TEMPORARY_AIRCRAFT_CAPACITY)
                .build();
    }

    private Flight findFlight(Long flightId) {
        validateId(flightId, "Flight ID");

        return flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flight not found with ID: " + flightId
                ));
    }

    private FlightInstance findFlightInstance(Long instanceId) {
        validateId(instanceId, "Flight instance ID");

        return flightInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flight instance not found with ID: " + instanceId
                ));
    }

    private void validateRequest(FlightInstanceRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight instance request is required"
            );
        }

        validateId(request.getFlightId(), "Flight ID");
        validateId(request.getScheduleId(), "Schedule ID");

        validateOptionalId(request.getAircraftId(), "Aircraft ID");

        validateSchedule(
                request.getDepartureDateTime(),
                request.getArrivalDateTime()
        );
    }

    private void validateSchedule(
            LocalDateTime departure,
            LocalDateTime arrival) {

        if (departure == null || arrival == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure and arrival date-times are required"
            );
        }

        if (!arrival.isAfter(departure)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Arrival must be after departure"
            );
        }
    }

    private void validateRoute(
            Long departureAirportId,
            Long arrivalAirportId) {

        if (departureAirportId == null || arrivalAirportId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight departure and arrival airports are required"
            );
        }

        if (departureAirportId.equals(arrivalAirportId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure and arrival airports must be different"
            );
        }
    }

    private void validateBookingWindow(
            Integer minDays,
            Integer maxDays) {

        if (minDays != null && minDays < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum advance booking days cannot be negative"
            );
        }

        if (maxDays != null && maxDays < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum advance booking days cannot be negative"
            );
        }

        if (minDays != null && maxDays != null
                && minDays > maxDays) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum booking days cannot exceed maximum booking days"
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

    private void validateOptionalId(Long id, String fieldName) {
        if (id != null && id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be a positive number"
            );
        }
    }

    private FlightSchedule findSchedule(Long scheduleId) {
        validateId(scheduleId, "Schedule ID");

        return flightScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule not found with ID: " + scheduleId
                ));
    }

    private void validateAirlineOwnership(Long airlineId, Flight flight) {
        if (!airlineId.equals(flight.getAirlineId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot create an instance for this airline"
            );
        }
    }

    private FlightInstance findOwnedInstance(Long airlineId, Long instanceId) {
        FlightInstance instance = findFlightInstance(instanceId);

        if (!airlineId.equals(instance.getAirlineId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This flight instance does not belong to the specified airline"
            );
        }

        return instance;
    }

    private void requireModifiable(FlightInstance instance) {
        if (LOCKED_STATUSES.contains(instance.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A flight instance that is " + instance.getStatus()
                            + " can no longer be modified"
            );
        }
    }

    // Same aircraft cannot fly two overlapping departures.
    private void validateAircraftAvailable(
            Long aircraftId,
            LocalDateTime departure,
            LocalDateTime arrival,
            Long excludeInstanceId) {

        if (flightInstanceRepository.existsAircraftConflict(
                aircraftId,
                FlightStatus.CANCELLED,
                excludeInstanceId,
                departure,
                arrival)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The aircraft is already assigned to another flight instance between "
                            + departure + " and " + arrival
            );
        }
    }
}
