
package com.sho1kat.service.impl;

import com.sho1kat.entity.Flight;
import com.sho1kat.entity.FlightInstance;
import com.sho1kat.enums.FlightStatus;
import com.sho1kat.mapper.FlightInstanceMapper;
import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.flightServiceDto.request.FlightInstanceRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightInstanceResponse;
import com.sho1kat.payload.response.AirportResponse;
import com.sho1kat.repository.FlightInstanceRepository;
import com.sho1kat.repository.FlightRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightInstanceServiceImpl implements FlightInstanceService {

    private final FlightInstanceRepository flightInstanceRepository;
    private final FlightRepository flightRepository;
    private final FlightInstanceMapper flightInstanceMapper;

    /*
     * TEMPORARY: replace with the aircraft-service client.
     * Aircraft capacity must come from the authoritative aircraft record.
     */
    private static final int TEMPORARY_AIRCRAFT_CAPACITY = 90;

    // Create flight instance
    @Override
    @Transactional
    public FlightInstanceResponse createFlightInstance(Long callerId, FlightInstanceRequest request) {

        validateId(callerId, "Caller ID");
        validateRequest(request);

        Flight flight = findFlight(request.getFlightId());

        if (!callerId.equals(flight.getAirlineId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot create an instance for this airline"
            );
        }

        validateRoute(
                flight.getDepartureAirportId(),
                flight.getArrivalAirportId()
        );

        validateBookingWindow(
                request.getMinAdvanceBookingDays(),
                request.getMaxAdvanceBookingDays()
        );

        FlightInstance instance =
                flightInstanceMapper.toEntity(request, flight);

        // A flight instance uses the route of its Flight.
        instance.setAirlineId(flight.getAirlineId());
        instance.setDepartureAirportId(
                flight.getDepartureAirportId()
        );
        instance.setArrivalAirportId(
                flight.getArrivalAirportId()
        );

        // New instances always start as scheduled.
        instance.setStatus(FlightStatus.SCHEDULED);

        // TODO: Fetch AircraftResponse using your service-to-service client.
        AircraftResponse aircraft = getAircraftForFlight(flight);

        Integer capacity = aircraft.getTotalSeats();

        if (capacity == null || capacity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Aircraft capacity is unavailable"
            );
        }

        instance.setTotalSeats(capacity);
        instance.setAvailableSeats(capacity);

        if (request.getIsActive() == null) {
            instance.setIsActive(true);
        }

        FlightInstance saved = flightInstanceRepository.save(instance);

        return convertToFlightInstanceResponse(saved);
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
            Long instanceId,
            FlightInstanceRequest request) {

        validateId(instanceId, "Flight instance ID");
        validateRequest(request);

        FlightInstance existing = findFlightInstance(instanceId);
        Flight newFlight = findFlight(request.getFlightId());

        validateRoute(
                newFlight.getDepartureAirportId(),
                newFlight.getArrivalAirportId()
        );

        validateBookingWindow(
                request.getMinAdvanceBookingDays(),
                request.getMaxAdvanceBookingDays()
        );

        int bookedSeats = existing.getTotalSeats()
                - existing.getAvailableSeats();

        if (bookedSeats < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid seat inventory"
            );
        }

        // Do not reassign an instance with existing bookings.
        if (!existing.getFlight().getId().equals(newFlight.getId())
                && bookedSeats > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot change the flight because seats have "
                            + "already been booked"
            );
        }

        AircraftResponse aircraft = getAircraftForFlight(newFlight);
        Integer capacity = aircraft.getTotalSeats();

        if (capacity == null || capacity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Aircraft capacity is unavailable"
            );
        }

        if (capacity < bookedSeats) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Aircraft capacity cannot be less than "
                            + "already-booked seats"
            );
        }

        flightInstanceMapper.updateEntity(
                request,
                existing,
                newFlight
        );

        // The instance route must match the associated Flight.
        existing.setAirlineId(newFlight.getAirlineId());
        existing.setDepartureAirportId(
                newFlight.getDepartureAirportId()
        );
        existing.setArrivalAirportId(
                newFlight.getArrivalAirportId()
        );

        // Do not let update requests overwrite seat inventory.
        existing.setTotalSeats(capacity);
        existing.setAvailableSeats(capacity - bookedSeats);

        validateSchedule(
                existing.getDepartureDateTime(),
                existing.getArrivalDateTime()
        );

        return convertToFlightInstanceResponse(
                flightInstanceRepository.save(existing)
        );
    }

    // Update flight instance status
    @Override
    @Transactional
    public FlightInstanceResponse updateFlightInstanceStatus(
            Long instanceId,
            FlightStatus status) {

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight status is required"
            );
        }

        FlightInstance instance = findFlightInstance(instanceId);

        instance.setStatus(status);

        return convertToFlightInstanceResponse(
                flightInstanceRepository.save(instance)
        );
    }

    // Activate or deactivate flight instance
    @Transactional
    @Override
    public FlightInstanceResponse updateFlightInstanceActiveStatus(
            Long instanceId,
            Boolean isActive) {

        if (isActive == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Active status is required"
            );
        }

        FlightInstance instance = findFlightInstance(instanceId);

        instance.setIsActive(isActive);

        return convertToFlightInstanceResponse(
                flightInstanceRepository.save(instance)
        );
    }

    // Delete flight instance
    @Override
    @Transactional
    public void deleteFlightInstance(Long instanceId) {

        FlightInstance instance = findFlightInstance(instanceId);

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
                .id(flight != null ? flight.getAircraftId() : null)
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
    private AircraftResponse getAircraftForFlight(Flight flight) {

        if (flight.getAircraftId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight has no assigned aircraft"
            );
        }

        return AircraftResponse.builder()
                .id(flight.getAircraftId())
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
}
