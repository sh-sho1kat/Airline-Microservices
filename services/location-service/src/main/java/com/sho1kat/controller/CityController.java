package com.sho1kat.controller;

import com.sho1kat.payload.request.CityRequest;
import com.sho1kat.payload.response.CityResponse;
import com.sho1kat.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @PostMapping
    public ResponseEntity<CityResponse> createCity(
            @Valid @RequestBody CityRequest cityRequest) {

        CityResponse response = cityService.createCity(cityRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityResponse> getCityById(
            @PathVariable Long id) {

        return ResponseEntity.ok(cityService.getCityById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CityResponse> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityRequest cityRequest) {

        return ResponseEntity.ok(
                cityService.updateCity(id, cityRequest)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCityById(
            @PathVariable Long id) {

        cityService.deleteCityById(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Page<CityResponse>> createCityInGroup(
            @Valid @RequestBody List<CityRequest> cityRequests) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cityService.createCityInGroup(cityRequests));
    }

    @GetMapping
    public ResponseEntity<Page<CityResponse>> getAllCities(
            Pageable pageable) {

        return ResponseEntity.ok(
                cityService.getAllCities(pageable)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CityResponse>> searchCities(@RequestParam String keyword, Pageable pageable) {

        return ResponseEntity.ok(
                cityService.searchCities(keyword, pageable)
        );
    }

    @GetMapping("/country/{countryCode}")
    public ResponseEntity<Page<CityResponse>> getCitiesByCountryCode(@PathVariable String countryCode, Pageable pageable) {

        return ResponseEntity.ok(
                cityService.getCitiesByCountryCode(countryCode, pageable)
        );
    }

    @GetMapping("/exists/{cityCode}")
    public ResponseEntity<Boolean> cityExists(@PathVariable String cityCode) {

        return ResponseEntity.ok(
                cityService.cityExists(cityCode)
        );
    }
}