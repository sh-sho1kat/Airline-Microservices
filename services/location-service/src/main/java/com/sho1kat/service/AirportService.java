package com.sho1kat.service;

import com.sho1kat.payload.request.AirportRequest;
import com.sho1kat.payload.response.AirportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AirportService {
    AirportResponse createAirport(AirportRequest airportRequest);

    Page<AirportResponse> getAllAirports(Pageable pageable);

    AirportResponse getAirportById(Long id);

    AirportResponse getAirportByIataCode(String iataCode);

    List<AirportResponse> getAirportsByCityId(Long cityId);

    AirportResponse updateAirport(
            Long id,
            AirportRequest airportRequest
    );

    void deleteAirportById(Long id);
}
