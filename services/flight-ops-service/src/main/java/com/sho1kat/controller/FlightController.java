
package com.sho1kat.controller;

import com.sho1kat.enums.FlightStatus;
import com.sho1kat.payload.flightServiceDto.request.FlightRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightResponse;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.service.FlightService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    // Create a flight
    @PostMapping
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody FlightRequest request) {

        FlightResponse response = flightService.createFlight(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get flight by database ID
    @GetMapping("/{flightId}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long flightId) {

        return ResponseEntity.ok(flightService.getFlightById(flightId));
    }

    // Get all flights with pagination
    @GetMapping
    public ResponseEntity<Page<FlightResponse>> getAllFlights(
            @PageableDefault(
                    size = 20,
                    sort = "flightNumber",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        return ResponseEntity.ok(flightService.getAllFlights(pageable));
    }

    // Update a flight by flight number
    @PutMapping("/{flightNumber}")
    public ResponseEntity<FlightResponse> updateFlight(@PathVariable String flightNumber, @Valid @RequestBody FlightRequest request) {

        return ResponseEntity.ok(flightService.updateFlight(flightNumber, request));
    }

    // Update flight status
    // Example: PATCH /api/v1/flights/BS101/status?status=DELAYED
    @PatchMapping("/{flightNumber}/status")
    public ResponseEntity<FlightResponse> updateFlightStatus(@PathVariable String flightNumber, @RequestParam FlightStatus status) {

        return ResponseEntity.ok(flightService.updateFlightStatus(flightNumber, status));
    }

    // Delete a flight belonging to a specific airline
    @DeleteMapping("/airlines/{flightNumber}")
    public ResponseEntity<MessageResponse> deleteFlight(@RequestHeader("airlineId") Long airlineId, @PathVariable String flightNumber) {

        flightService.deleteFlight(airlineId, flightNumber);

        return ResponseEntity.ok(new MessageResponse("Flight deleted successfully."));
    }

    // Search flights by route and optional departure date
    @GetMapping("/search")
    public ResponseEntity<Page<FlightResponse>> searchFlights(
            @RequestParam(required = false) Long departureAirportId,
            @RequestParam(required = false) Long arrivalAirportId,
            @RequestParam(required = false) LocalDate departureDate,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                flightService.searchFlights(
                        departureAirportId,
                        arrivalAirportId,
                        departureDate,
                        pageable
                )
        );
    }

    // Search flights by airline
    @GetMapping("/search/airline/{airlineId}")
    public ResponseEntity<Page<FlightResponse>> searchFlightsByAirline(
            @PathVariable Long airlineId,
            @RequestParam(required = false) Long departureAirportId,
            @RequestParam(required = false) Long arrivalAirportId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                flightService.searchFlightsByAirline(
                        airlineId,
                        departureAirportId,
                        arrivalAirportId,
                        pageable
                )
        );
    }

    // Search flights by aircraft
    @GetMapping("/search/aircraft/{aircraftId}")
    public ResponseEntity<Page<FlightResponse>> searchFlightsByAircraft(
            @PathVariable Long aircraftId,
            @RequestParam(required = false) Long departureAirportId,
            @RequestParam(required = false) Long arrivalAirportId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                flightService.searchFlightsByAircraft(
                        aircraftId,
                        departureAirportId,
                        arrivalAirportId,
                        pageable
                )
        );
    }

    // Search flights by departure date
    @GetMapping("/search/date")
    public ResponseEntity<Page<FlightResponse>> searchFlightsByDate(
            @RequestParam LocalDate departureDate,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(flightService.searchFlightsByDate(departureDate, pageable)
        );
    }

    // Search flights by status
    @GetMapping("/search/status")
    public ResponseEntity<Page<FlightResponse>> searchFlightsByStatus(
            @RequestParam FlightStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(flightService.searchFlightsByStatus(status, pageable));
    }
}
