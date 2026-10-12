package com.sho1kat.controller;

import com.sho1kat.entity.Aircraft;
import com.sho1kat.enums.AircraftStatus;
import com.sho1kat.payload.airlineServiceDto.request.AircraftRequest;
import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AircraftStatusResponse;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.service.AircraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/aircraft")
@RequiredArgsConstructor
public class AircraftController {

    private final AircraftService aircraftService;

    @PostMapping("/create")
    public ResponseEntity<AircraftResponse> createAircraft(
            @Valid @RequestBody AircraftRequest request,
            @RequestHeader("X-User-Id") Long callerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aircraftService.createAircraft(request, callerId));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<AircraftResponse>> getAllAircraft(
            @RequestParam(required = false) Long airlineId,
            @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        return ResponseEntity.ok(aircraftService.listAircraftByAirline(airlineId, pageable));
    }

    @GetMapping("/status")
    public ResponseEntity<Page<AircraftResponse>> getAircraftByStatus(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        AircraftStatus aircraftStatus = null;
        if (status != null) {
            try {
                aircraftStatus = AircraftStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(aircraftService.listAircraft(aircraftStatus, pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<AircraftResponse>> getAircraftByAirlineAndStatus(
            @RequestParam(required = false) Long airlineId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        AircraftStatus aircraftStatus = null;
        if (status != null) {
            try {
                aircraftStatus = AircraftStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(aircraftService.listAircraftByAirlineAndStatus(airlineId, aircraftStatus, pageable));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<AircraftStatusResponse> getAircraftStatus(@PathVariable Long id) {
        return ResponseEntity.ok(aircraftService.getAircraftStatus(id));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<AircraftStatusResponse> activateAircraft(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long callerId) {
        return ResponseEntity.ok(aircraftService.updateAircraftStatus(id, AircraftStatus.ACTIVE, callerId));
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<AircraftStatusResponse> setAircraftMaintenance(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long callerId) {
        return ResponseEntity.ok(aircraftService.updateAircraftStatus(id, AircraftStatus.MAINTENANCE, callerId));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<AircraftStatusResponse> deactivateAircraft(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long callerId) {
        return ResponseEntity.ok(aircraftService.updateAircraftStatus(id, AircraftStatus.INACTIVE, callerId));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<AircraftStatusResponse> retireAircraft(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long callerId) {
        return ResponseEntity.ok(aircraftService.updateAircraftStatus(id, AircraftStatus.RETIRED, callerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AircraftResponse> getAircraftById(@PathVariable Long id) {
        return ResponseEntity.ok(aircraftService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AircraftResponse> updateAircraft(
            @PathVariable Long id,
            @Valid @RequestBody AircraftRequest request,
            @RequestHeader("X-User-Id") Long callerId) {
        return ResponseEntity.ok(aircraftService.updateAircraft(id, request, callerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteAircraft(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long callerId) {
        aircraftService.deleteAircraft(id, callerId);
        return ResponseEntity.ok(new MessageResponse("Aircraft deleted successfully."));
    }

}
