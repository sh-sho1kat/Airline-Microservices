
package com.sho1kat.controller;

import com.sho1kat.enums.FlightStatus;
import com.sho1kat.payload.flightServiceDto.request.FlightInstanceRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightInstanceResponse;
import com.sho1kat.service.FlightInstanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/flight-instances")
@RequiredArgsConstructor
public class FlightInstanceController {

    private final FlightInstanceService flightInstanceService;

    // Create a flight instance
    @PostMapping
    public ResponseEntity<FlightInstanceResponse> createFlightInstance(
            @RequestHeader("X-Airline-Id") Long callerId,
            @Valid @RequestBody FlightInstanceRequest request) {

        FlightInstanceResponse response =
                flightInstanceService.createFlightInstance(
                        callerId,
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // Get a flight instance by ID
    @GetMapping("/{instanceId}")
    public ResponseEntity<FlightInstanceResponse> getFlightInstanceById(
            @PathVariable Long instanceId) {

        return ResponseEntity.ok(
                flightInstanceService.getFlightInstanceById(instanceId)
        );
    }

    // Get all flight instances with pagination
    @GetMapping
    public ResponseEntity<Page<FlightInstanceResponse>> getAllFlightInstances(
            @PageableDefault(
                    size = 20,
                    sort = "departureDateTime",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                flightInstanceService.getAllFlightInstances(pageable)
        );
    }

    // Update flight instance details
    @PutMapping("/{instanceId}")
    public ResponseEntity<FlightInstanceResponse> updateFlightInstance(
            @RequestHeader("X-Airline-Id") Long airlineId,
            @PathVariable Long instanceId,
            @Valid @RequestBody FlightInstanceRequest request) {

        return ResponseEntity.ok(
                flightInstanceService.updateFlightInstance(
                        airlineId,
                        instanceId,
                        request
                )
        );
    }

    // Update operational status
    public ResponseEntity<FlightInstanceResponse> updateStatus(
            @RequestHeader("X-Airline-Id") Long airlineId,
            @PathVariable Long instanceId,
            @RequestParam FlightStatus status) {

        return ResponseEntity.ok(
                flightInstanceService.updateFlightInstanceStatus(
                        airlineId,
                        instanceId,
                        status
                )
        );
    }

    // Activate or deactivate a flight instance
    @PatchMapping("/{instanceId}/active")
    public ResponseEntity<FlightInstanceResponse> updateActiveStatus(
            @RequestHeader("X-Airline-Id") Long airlineId,
            @PathVariable Long instanceId,
            @RequestParam Boolean isActive) {

        return ResponseEntity.ok(
                flightInstanceService.updateFlightInstanceActiveStatus(
                        airlineId,
                        instanceId,
                        isActive
                )
        );
    }

    // Delete a flight instance
    @DeleteMapping("/{instanceId}")
    public ResponseEntity<Void> deleteFlightInstance(
            @RequestHeader("X-Airline-Id") Long airlineId,
            @PathVariable Long instanceId) {

        flightInstanceService.deleteFlightInstance(airlineId, instanceId);

        return ResponseEntity.noContent().build();
    }

    // Search using optional filters
    @GetMapping("/search")
    public ResponseEntity<Page<FlightInstanceResponse>> searchFlightInstances(
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) Long airlineId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate departureDate,
            @RequestParam(required = false) Long departureAirportId,
            @RequestParam(required = false) Long arrivalAirportId,
            @RequestParam(required = false) FlightStatus status,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(
                    size = 20,
                    sort = "departureDateTime",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                flightInstanceService.searchFlightInstances(
                        flightId,
                        airlineId,
                        departureDate,
                        departureAirportId,
                        arrivalAirportId,
                        status,
                        isActive,
                        pageable
                )
        );
    }
}
