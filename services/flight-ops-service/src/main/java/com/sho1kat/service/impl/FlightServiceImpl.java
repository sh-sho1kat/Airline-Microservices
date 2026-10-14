package com.sho1kat.service.impl;

import com.sho1kat.entity.Flight;
import com.sho1kat.enums.FlightStatus;
import com.sho1kat.mapper.FlightMapper;
import com.sho1kat.payload.flightServiceDto.request.FlightRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightResponse;
import com.sho1kat.repository.FlightRepository;
import com.sho1kat.service.FlightService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    @Override
    @Transactional
    public FlightResponse createFlight(FlightRequest request) {
        validateCreateRequest(request);

        String flightNumber = request.getFlightNumber().trim();

        if (flightRepository.existsByFlightNumberIgnoreCase(
                flightNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Flight number already exists: " + flightNumber
            );
        }

        Flight flight = flightMapper.toEntity(request);
        flight.setFlightNumber(flightNumber);

        return flightMapper.toResponse(
                flightRepository.save(flight)
        );
    }

    @Override
    public FlightResponse getFlightById(Long flightId) {
        validateId(flightId, "Flight ID");

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flight not found with ID: " + flightId
                ));

        return flightMapper.toResponse(flight);
    }

    @Override
    public Page<FlightResponse> getAllFlights(Pageable pageable) {
        return flightRepository.findAll(pageable)
                .map(flightMapper::toResponse);
    }

    @Override
    @Transactional
    public FlightResponse updateFlight(String flightNumber, FlightRequest request) {

        Flight existing = findFlightByNumber(flightNumber);

        validateUpdateRequest(request, existing);

        if (request.getFlightNumber() != null) {
            String newFlightNumber = request.getFlightNumber().trim();

            boolean duplicate = flightRepository.existsByFlightNumberIgnoreCaseAndIdNot(newFlightNumber, existing.getId());

            if (duplicate) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Flight number already exists: " + newFlightNumber
                );
            }
        }

        flightMapper.updateEntity(request, existing);

        validateRoute(
                existing.getDepartureAirportId(),
                existing.getArrivalAirportId()
        );

        return flightMapper.toResponse(flightRepository.save(existing));
    }

    @Override
    @Transactional
    public FlightResponse updateFlightStatus(String flightNumber, FlightStatus status) {

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight status is required"
            );
        }

        Flight flight = findFlightByNumber(flightNumber);
        flight.setFlightStatus(status);

        return flightMapper.toResponse(flightRepository.save(flight));
    }

    @Override
    @Transactional
    public void deleteFlight(Long airlineId, String flightNumber) {

        validateId(airlineId, "Airline ID");

        if (flightNumber == null || flightNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight number is required"
            );
        }

        Flight flight = flightRepository.findByAirlineIdAndFlightNumberIgnoreCase(airlineId, flightNumber.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flight not found for airline ID: "
                                + airlineId + " and flight number: "
                                + flightNumber
                ));

        flightRepository.delete(flight);
    }

    @Override
    public Page<FlightResponse> searchFlights(
            Long departureAirportId,
            Long arrivalAirportId,
            LocalDate departureDate,
            Pageable pageable) {

        Specification<Flight> spec = buildSpecification(
                departureAirportId,
                arrivalAirportId,
                departureDate,
                null,
                null,
                null
        );

        return flightRepository.findAll(spec, pageable).map(flightMapper::toResponse);
    }

    @Override
    public Page<FlightResponse> searchFlightsByAirline(
            Long airlineId,
            Long departureAirportId,
            Long arrivalAirportId,
            Pageable pageable) {

        validateId(airlineId, "Airline ID");

        Specification<Flight> spec = buildSpecification(
                departureAirportId,
                arrivalAirportId,
                null,
                airlineId,
                null,
                null
        );

        return flightRepository.findAll(spec, pageable)
                .map(flightMapper::toResponse);
    }

    @Override
    public Page<FlightResponse> searchFlightsByAircraft(
            Long aircraftId,
            Long departureAirportId,
            Long arrivalAirportId,
            Pageable pageable) {

        validateId(aircraftId, "Aircraft ID");

        Specification<Flight> spec = buildSpecification(
                departureAirportId,
                arrivalAirportId,
                null,
                null,
                aircraftId,
                null
        );

        return flightRepository.findAll(spec, pageable).map(flightMapper::toResponse);
    }

    @Override
    public Page<FlightResponse> searchFlightsByDate(LocalDate departureDate, Pageable pageable) {

        if (departureDate == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure date is required"
            );
        }

        Specification<Flight> spec = buildSpecification(
                null,
                null,
                departureDate,
                null,
                null,
                null
        );

        return flightRepository.findAll(spec, pageable)
                .map(flightMapper::toResponse);
    }

    @Override
    public Page<FlightResponse> searchFlightsByStatus(FlightStatus status, Pageable pageable) {

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight status is required"
            );
        }

        Specification<Flight> spec = buildSpecification(
                null,
                null,
                null,
                null,
                null,
                status
        );

        return flightRepository.findAll(spec, pageable).map(flightMapper::toResponse);
    }

    private Flight findFlightByNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight number is required"
            );
        }

        return flightRepository
                .findByFlightNumberIgnoreCase(flightNumber.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flight not found: " + flightNumber
                ));
    }

    private void validateCreateRequest(FlightRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight request is required"
            );
        }

        if (request.getFlightNumber() == null
                || request.getFlightNumber().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight number is required"
            );
        }

        validateId(request.getAirlineId(), "Airline ID");
        validateId(request.getAircraftId(), "Aircraft ID");
        validateId(
                request.getDepartureAirportId(),
                "Departure airport ID"
        );
        validateId(
                request.getArrivalAirportId(),
                "Arrival airport ID"
        );

        validateRoute(
                request.getDepartureAirportId(),
                request.getArrivalAirportId()
        );
    }

    private void validateUpdateRequest(FlightRequest request, Flight existing) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight request is required"
            );
        }

        if (request.getFlightNumber() != null
                && request.getFlightNumber().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight number cannot be blank"
            );
        }

        Long departureAirportId =
                request.getDepartureAirportId() != null
                        ? request.getDepartureAirportId()
                        : existing.getDepartureAirportId();

        Long arrivalAirportId =
                request.getArrivalAirportId() != null
                        ? request.getArrivalAirportId()
                        : existing.getArrivalAirportId();

        validateRoute(departureAirportId, arrivalAirportId);
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be a positive number"
            );
        }
    }

    private void validateRoute(
            Long departureAirportId,
            Long arrivalAirportId) {

        if (departureAirportId == null || arrivalAirportId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure and arrival airports are required"
            );
        }

        if (departureAirportId.equals(arrivalAirportId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure and arrival airports must be different"
            );
        }
    }

    private void validateSchedule(
            LocalDateTime departureTime,
            LocalDateTime arrivalTime) {

        if (departureTime == null || arrivalTime == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure and arrival times are required"
            );
        }

        if (!arrivalTime.isAfter(departureTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Arrival time must be after departure time"
            );
        }
    }

    private Specification<Flight> buildSpecification(
            Long departureAirportId,
            Long arrivalAirportId,
            LocalDate departureDate,
            Long airlineId,
            Long aircraftId,
            FlightStatus status) {

        Specification<Flight> spec =
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (departureAirportId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("departureAirportId"), departureAirportId));
        }

        if (arrivalAirportId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("arrivalAirportId"), arrivalAirportId));
        }

        if (departureDate != null) {
            LocalDateTime start = departureDate.atStartOfDay();
            LocalDateTime end = departureDate.plusDays(1).atStartOfDay();

            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.and(
                            criteriaBuilder.greaterThanOrEqualTo(root.<LocalDateTime>get("departureTime"), start),
                            criteriaBuilder.lessThan(root.<LocalDateTime>get("departureTime"), end)
                    ));
        }

        if (airlineId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("airlineId"), airlineId));
        }

        if (aircraftId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("aircraftId"), aircraftId));
        }

        if (status != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("flightStatus"), status));
        }

        return spec;
    }
}