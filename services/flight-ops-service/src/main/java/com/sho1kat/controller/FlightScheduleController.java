package com.sho1kat.controller;

import com.sho1kat.payload.flightServiceDto.request.FlightScheduleRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightScheduleResponse;
import com.sho1kat.service.FlightScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flight-schedules")
@RequiredArgsConstructor
public class FlightScheduleController {

    private final FlightScheduleService flightScheduleService;

    @PostMapping
    public ResponseEntity<FlightScheduleResponse> createFlightSchedule(
            @RequestHeader("X-Airline-Id") Long airlineId,
            @Valid @RequestBody FlightScheduleRequest request) {

        FlightScheduleResponse response =
                flightScheduleService.createFlightSchedule(
                        airlineId,
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<FlightScheduleResponse> getFlightScheduleById(
            @RequestHeader("X-Airline-Id") Long airlineId,
            @PathVariable Long scheduleId) {

        return ResponseEntity.ok(
                flightScheduleService.getFlightScheduleById(
                        airlineId,
                        scheduleId
                )
        );
    }

    @GetMapping("/airline")
    public ResponseEntity<List<FlightScheduleResponse>>
    getFlightSchedulesByAirline(
            @RequestHeader("X-Airline-Id") Long airlineId) {

        return ResponseEntity.ok(
                flightScheduleService.getFlightScheduleByAirline(airlineId)
        );
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<FlightScheduleResponse> updateFlightSchedule(
            @RequestHeader("X-Airline-Id") Long airlineId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody FlightScheduleRequest request) {

        return ResponseEntity.ok(
                flightScheduleService.updateFlightSchedule(
                        airlineId,
                        scheduleId,
                        request
                )
        );
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteFlightSchedule(
            @RequestHeader("X-Airline-Id") Long airlineId,
            @PathVariable Long scheduleId) {

        flightScheduleService.deleteFlightSchedule(
                airlineId,
                scheduleId
        );

        return ResponseEntity.noContent().build();
    }
}