
package com.sho1kat.controller;

import com.sho1kat.payload.pricingServiceDto.request.FlightInstanceSyncRequest;
import com.sho1kat.payload.pricingServiceDto.response.FlightInstanceSyncResponse;
import com.sho1kat.service.FlightInstanceReferenceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/*
 * Internal endpoint for Flight Ops lifecycle events. Calls are safe to retry;
 * they upsert the local read model and only apply templates on first registration.
 */
@RestController
@RequestMapping("/api/v1/internal/flight-instances")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_FLIGHT_OPS_SERVICE', 'ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
public class FlightInstanceSyncController {

    private final FlightInstanceReferenceService flightInstanceReferenceService;

    // Upsert flight-instance read-model data published by Flight Ops
    @PutMapping
    public ResponseEntity<FlightInstanceSyncResponse> upsertFlightInstance(
            @Valid @RequestBody FlightInstanceSyncRequest request) {

        return ResponseEntity.ok(
                flightInstanceReferenceService.upsertFromFlightOps(request)
        );
    }
}
