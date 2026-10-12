package com.sho1kat.controller;

import com.sho1kat.enums.AirlineStatus;
import com.sho1kat.payload.airlineServiceDto.request.AirlineRequest;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.dto.AirlineDropdownItem;
import com.sho1kat.service.AirlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/airline")
public class AirlineController {
    private final AirlineService airlineService;

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<AirlineResponse> createAirline(
            @Valid @RequestBody AirlineRequest request,
            @RequestHeader("X-User-Id") Long actorId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airlineService.createAirline(request, actorId));
    }

    @GetMapping("/all")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<AirlineResponse>> getAllAirlines(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(airlineService.getAllAirlines(pageable));
    }

    @GetMapping("/dropdown")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AirlineDropdownItem>> getAirlineDropdown() {
        return ResponseEntity.ok(airlineService.getAirlineDropdown());
    }

    @Transactional(readOnly = true)
    @GetMapping("/iata/{code}")
    public ResponseEntity<AirlineResponse> getAirlineByIataCode(@PathVariable String code) {
        return ResponseEntity.ok(airlineService.getAirlineByIataCode(code));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<AirlineResponse> getAirlineById(@PathVariable Long id) {
        return ResponseEntity.ok(airlineService.getAirlineById(id));
    }

    @GetMapping("/owner")
    @Transactional(readOnly = true)
    public ResponseEntity<AirlineResponse> getAirlineByOwner(@RequestParam Long ownerId) {
        return ResponseEntity.ok(airlineService.getAirlineByOwner(ownerId));
    }

    @GetMapping("/status")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<AirlineResponse>> getAllAirlinesByStatus(
            @RequestParam AirlineStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(airlineService.getAllAirlinesByStatus(status, pageable));
    }

    @Transactional
    @PutMapping("/update/{id}")
    public ResponseEntity<AirlineResponse> updateAirline(
            @PathVariable Long id,
            @Valid @RequestBody AirlineRequest request,
            @RequestHeader("X-User-Id") Long actorId) {
        return ResponseEntity.ok(airlineService.updateAirline(id, request, actorId));
    }

    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAirline(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long actorId) {
        airlineService.deleteAirline(id, actorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AirlineResponse> approveAirline(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long actorId) {
        return ResponseEntity.ok(airlineService.changeStatusByAdmin(id, AirlineStatus.ACTIVE, actorId));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<AirlineResponse> suspendAirline(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long actorId) {
        return ResponseEntity.ok(airlineService.changeStatusByAdmin(id, AirlineStatus.INACTIVE, actorId));
    }

    @PostMapping("/{id}/ban")
    public ResponseEntity<AirlineResponse> banAirline(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long actorId) {
        return ResponseEntity.ok(airlineService.changeStatusByAdmin(id, AirlineStatus.BANNED, actorId));
    }
}
