
package com.sho1kat.controller;

import com.sho1kat.payload.pricingServiceDto.request.FareTemplateRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareTemplateResponse;
import com.sho1kat.service.FareTemplateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fare-templates")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
public class FareTemplateController {

    private final FareTemplateService fareTemplateService;

    // Create a fare template
    @PostMapping
    public ResponseEntity<FareTemplateResponse> createFareTemplate(
            @Valid @RequestBody FareTemplateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fareTemplateService.createFareTemplate(request));
    }

    // Get all fare templates
    @GetMapping
    public ResponseEntity<List<FareTemplateResponse>> getAllFareTemplates() {

        return ResponseEntity.ok(
                fareTemplateService.getAllFareTemplates()
        );
    }

    // Get a fare template by ID
    @GetMapping("/{id}")
    public ResponseEntity<FareTemplateResponse> getFareTemplateById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                fareTemplateService.getFareTemplateById(id)
        );
    }

    // Update a fare template
    @PutMapping("/{id}")
    public ResponseEntity<FareTemplateResponse> updateFareTemplate(
            @PathVariable Long id,
            @Valid @RequestBody FareTemplateRequest request) {

        return ResponseEntity.ok(
                fareTemplateService.updateFareTemplate(id, request)
        );
    }

    // Deactivate a fare template (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateFareTemplate(
            @PathVariable Long id) {

        fareTemplateService.deactivateFareTemplate(id);

        return ResponseEntity.noContent().build();
    }
}
