package com.sho1kat.service.impl;

import com.sho1kat.entity.Flight;
import com.sho1kat.mapper.FlightMapper;
import com.sho1kat.payload.flightServiceDto.request.FlightRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightResponse;
import com.sho1kat.repository.FlightInstanceRepository;
import com.sho1kat.repository.FlightRepository;
import com.sho1kat.repository.FlightScheduleRepository;
import com.sho1kat.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final FlightScheduleRepository scheduleRepository;
    private final FlightInstanceRepository instanceRepository;

    @Override
    @Transactional
    public FlightResponse createFlight(Long airlineId, FlightRequest request) {
        validateId(airlineId, "Airline ID");
        validateCreateRequest(request);

        String flightNumber = normalizeFlightNumber(request.getFlightNumber());

        if (flightRepository.existsByFlightNumberIgnoreCase(flightNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Flight number already exists: " + flightNumber
            );
        }

        return flightMapper.toResponse(
                flightRepository.save(flightMapper.toEntity(request, airlineId))
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
    public FlightResponse updateFlight(Long airlineId, String flightNumber, FlightRequest request) {

        validateId(airlineId, "Airline ID");

        Flight existing = findOwnedFlight(airlineId, flightNumber);

        validateUpdateRequest(request, existing);

        if (request.getFlightNumber() != null) {
            String newFlightNumber = normalizeFlightNumber(request.getFlightNumber());

            if (flightRepository.existsByFlightNumberIgnoreCaseAndIdNot(
                    newFlightNumber, existing.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Flight number already exists: " + newFlightNumber
                );
            }
        }

        Long newDeparture = request.getDepartureAirportId() != null
                ? request.getDepartureAirportId()
                : existing.getDepartureAirportId();
        Long newArrival = request.getArrivalAirportId() != null
                ? request.getArrivalAirportId()
                : existing.getArrivalAirportId();

        boolean routeChanged = !newDeparture.equals(existing.getDepartureAirportId())
                || !newArrival.equals(existing.getArrivalAirportId());

        if (routeChanged && hasSchedulesOrInstances(existing.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The route cannot be changed because schedules or flight "
                            + "instances already use this flight. Create a new flight."
            );
        }

        flightMapper.updateEntity(request, existing);

        return flightMapper.toResponse(flightRepository.save(existing));
    }

    @Override
    @Transactional
    public FlightResponse updateFlightActiveStatus(Long airlineId, String flightNumber, Boolean isActive) {

        validateId(airlineId, "Airline ID");

        if (isActive == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Active status is required"
            );
        }

        Flight flight = findOwnedFlight(airlineId, flightNumber);
        flight.setIsActive(isActive);

        return flightMapper.toResponse(flightRepository.save(flight));
    }

    @Override
    @Transactional
    public void deleteFlight(Long airlineId, String flightNumber) {

        validateId(airlineId, "Airline ID");

        Flight flight = findOwnedFlight(airlineId, flightNumber);

        if (hasSchedulesOrInstances(flight.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This flight has schedules or flight instances. "
                            + "Deactivate it instead of deleting it."
            );
        }

        flightRepository.delete(flight);
    }

    @Override
    public Page<FlightResponse> searchFlights(
            Long departureAirportId,
            Long arrivalAirportId,
            Pageable pageable) {

        return flightRepository.findAll(
                buildSpecification(departureAirportId, arrivalAirportId, null, null),
                pageable
        ).map(flightMapper::toResponse);
    }

    @Override
    public Page<FlightResponse> searchFlightsByAirline(
            Long airlineId,
            Long departureAirportId,
            Long arrivalAirportId,
            Pageable pageable) {

        validateId(airlineId, "Airline ID");

        return flightRepository.findAll(
                buildSpecification(departureAirportId, arrivalAirportId, airlineId, null),
                pageable
        ).map(flightMapper::toResponse);
    }

    @Override
    public Page<FlightResponse> searchFlightsByAircraft(
            Long aircraftId,
            Long departureAirportId,
            Long arrivalAirportId,
            Pageable pageable) {

        validateId(aircraftId, "Aircraft ID");

        return flightRepository.findAll(
                buildSpecification(departureAirportId, arrivalAirportId, null, aircraftId),
                pageable
        ).map(flightMapper::toResponse);
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

//        validateId(request.getAirlineId(), "Airline ID");
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

    private Flight findOwnedFlight(Long airlineId, String flightNumber) {
        Flight flight = findFlightByNumber(flightNumber);

        if (!airlineId.equals(flight.getAirlineId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This flight does not belong to the specified airline"
            );
        }

        return flight;
    }

    private boolean hasSchedulesOrInstances(Long flightId) {
        return scheduleRepository.existsByFlight_Id(flightId)
                || instanceRepository.existsByFlight_Id(flightId);
    }

    private String normalizeFlightNumber(String flightNumber) {
        return flightNumber.trim().toUpperCase(Locale.ROOT);
    }

//

    private Specification<Flight> buildSpecification(
            Long departureAirportId,
            Long arrivalAirportId,
            Long airlineId,
            Long aircraftId) {

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

        if (airlineId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("airlineId"), airlineId));
        }

        if (aircraftId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("aircraftId"), aircraftId));
        }

        return spec;
    }
}