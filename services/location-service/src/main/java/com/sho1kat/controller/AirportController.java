package com.sho1kat.controller;

import com.sho1kat.payload.request.AirportRequest;
import com.sho1kat.payload.response.AirportResponse;
import com.sho1kat.service.AirportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @PostMapping
    public ResponseEntity<AirportResponse> createAirport(@Valid @RequestBody AirportRequest airportRequest) {
        AirportResponse response = airportService.createAirport(airportRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportResponse> getAirportById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                airportService.getAirportById(id)
        );
    }

    @GetMapping("/iata/{iataCode}")
    public ResponseEntity<AirportResponse> getAirportByIataCode(@PathVariable String iataCode) {

        return ResponseEntity.ok(
                airportService.getAirportByIataCode(iataCode)
        );
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<AirportResponse>> getAirportsByCity(@PathVariable Long cityId) {

        return ResponseEntity.ok(
                airportService.getAirportsByCityId(cityId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirportResponse> updateAirport(@PathVariable Long id, @Valid @RequestBody AirportRequest airportRequest) {

        return ResponseEntity.ok(
                airportService.updateAirport(
                        id,
                        airportRequest
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAirport(@PathVariable Long id) {

        airportService.deleteAirportById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<AirportResponse>> getAllAirports(Pageable pageable) {

        return ResponseEntity.ok(
                airportService.getAllAirports(pageable)
        );
    }
}