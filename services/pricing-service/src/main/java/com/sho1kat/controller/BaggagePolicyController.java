
package com.sho1kat.controller;

import com.sho1kat.enums.CabinClass;
import com.sho1kat.payload.pricingServiceDto.request.BaggagePolicyRequest;
import com.sho1kat.payload.pricingServiceDto.response.BaggagePolicyResponse;
import com.sho1kat.service.BaggagePolicyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/baggage-policies")
@RequiredArgsConstructor
public class BaggagePolicyController {

    private final BaggagePolicyService baggagePolicyService;

    // Create a baggage policy
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<BaggagePolicyResponse> createBaggagePolicy(
            @Valid @RequestBody BaggagePolicyRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(baggagePolicyService.createBaggagePolicy(request));
    }

    // Public airline/cabin lookup of the active policy
    @GetMapping("/lookup")
    public ResponseEntity<BaggagePolicyResponse> findActiveForAirlineAndCabin(
            @RequestParam Long airlineId,
            @RequestParam CabinClass cabin) {

        return ResponseEntity.ok(
                baggagePolicyService.findActiveForAirlineAndCabin(airlineId, cabin)
        );
    }

    // Get all baggage policies with an optional airline filter
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<List<BaggagePolicyResponse>> getAllBaggagePolicies(
            @RequestParam(required = false) Long airlineId) {

        return ResponseEntity.ok(
                baggagePolicyService.getAllBaggagePolicies(airlineId)
        );
    }

    // Update a baggage policy
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<BaggagePolicyResponse> updateBaggagePolicy(
            @PathVariable Long id,
            @Valid @RequestBody BaggagePolicyRequest request) {

        return ResponseEntity.ok(
                baggagePolicyService.updateBaggagePolicy(id, request)
        );
    }

    // Deactivate a baggage policy (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<Void> deactivateBaggagePolicy(
            @PathVariable Long id) {

        baggagePolicyService.deactivateBaggagePolicy(id);

        return ResponseEntity.noContent().build();
    }
}
