
package com.sho1kat.controller;

import com.sho1kat.enums.CabinClass;
import com.sho1kat.payload.pricingServiceDto.request.FareRequest;
import com.sho1kat.payload.pricingServiceDto.request.LowestFareRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareResponse;
import com.sho1kat.payload.pricingServiceDto.response.LowestFareResponse;
import com.sho1kat.payload.pricingServiceDto.response.QuoteResponse;
import com.sho1kat.payload.pricingServiceDto.response.UnpricedFlightResponse;
import com.sho1kat.service.FareQuoteService;
import com.sho1kat.service.FareService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fares")
@RequiredArgsConstructor
public class FareController {

    private final FareService fareService;
    private final FareQuoteService fareQuoteService;

    // Create a fare
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<FareResponse> createFare(
            @Valid @RequestBody FareRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fareService.createFare(request));
    }

    // Create fares in bulk (1-200, atomically)
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<List<FareResponse>> createFaresInBulk(
            @RequestBody List<@Valid FareRequest> requests) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fareService.createFaresInBulk(requests));
    }

    // Find synced active flight instances without any active fare
    @GetMapping("/unpriced")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<List<UnpricedFlightResponse>> findUnpricedInstances(
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        return ResponseEntity.ok(
                fareService.findUnpricedInstances(flightId, from, to)
        );
    }

    // List current active fares for an instance
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<List<FareResponse>> getFaresByFlightInstanceId(
            @RequestParam Long flightInstanceId) {

        return ResponseEntity.ok(
                fareService.getFaresByFlightInstanceId(flightInstanceId)
        );
    }

    // Quote passenger pricing; fare class and coupon are optional (public lookup)
    @GetMapping("/quote")
    public ResponseEntity<QuoteResponse> quoteFare(
            @RequestParam Long flightInstanceId,
            @RequestParam CabinClass cabin,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "0") int children,
            @RequestParam(defaultValue = "0") int infants,
            @RequestParam(required = false) String fareClass,
            @RequestParam(required = false) String couponCode) {

        return ResponseEntity.ok(
                fareQuoteService.quoteFare(
                        flightInstanceId,
                        cabin,
                        adults,
                        children,
                        infants,
                        fareClass,
                        couponCode
                )
        );
    }

    // Lowest current fare for up to 200 instances and a cabin (public lookup)
    @PostMapping("/lowest")
    public ResponseEntity<LowestFareResponse> findLowestFares(
            @Valid @RequestBody LowestFareRequest request) {

        return ResponseEntity.ok(
                fareService.findLowestFares(request)
        );
    }

    // Get a fare by ID
    @GetMapping("/{fareId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<FareResponse> getFareById(
            @PathVariable Long fareId) {

        return ResponseEntity.ok(
                fareService.getFareById(fareId)
        );
    }

    // Update a fare; flightInstanceId is immutable
    @PutMapping("/{fareId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<FareResponse> updateFare(
            @PathVariable Long fareId,
            @Valid @RequestBody FareRequest request) {

        return ResponseEntity.ok(
                fareService.updateFare(fareId, request)
        );
    }

    // Deactivate a fare (soft delete)
    @DeleteMapping("/{fareId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<Void> deactivateFare(
            @PathVariable Long fareId) {

        fareService.deactivateFare(fareId);

        return ResponseEntity.noContent().build();
    }
}
