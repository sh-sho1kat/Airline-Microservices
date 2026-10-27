
package com.sho1kat.controller;

import com.sho1kat.payload.pricingServiceDto.request.FareRuleRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareRuleResponse;
import com.sho1kat.service.FareRuleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fare-rules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
public class FareRuleController {

    private final FareRuleService fareRuleService;

    // Create a fare rule
    @PostMapping
    public ResponseEntity<FareRuleResponse> createFareRule(
            @Valid @RequestBody FareRuleRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fareRuleService.createFareRule(request));
    }

    // Get all fare rules
    @GetMapping
    public ResponseEntity<List<FareRuleResponse>> getAllFareRules() {

        return ResponseEntity.ok(
                fareRuleService.getAllFareRules()
        );
    }

    // Get a fare rule by ID
    @GetMapping("/{id}")
    public ResponseEntity<FareRuleResponse> getFareRuleById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                fareRuleService.getFareRuleById(id)
        );
    }

    // Update a fare rule
    @PutMapping("/{id}")
    public ResponseEntity<FareRuleResponse> updateFareRule(
            @PathVariable Long id,
            @Valid @RequestBody FareRuleRequest request) {

        return ResponseEntity.ok(
                fareRuleService.updateFareRule(id, request)
        );
    }

    // Delete deactivates; it does not physically delete rule history
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateFareRule(
            @PathVariable Long id) {

        fareRuleService.deactivateFareRule(id);

        return ResponseEntity.noContent().build();
    }
}
