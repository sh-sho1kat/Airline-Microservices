package com.sho1kat.service.impl;

import com.sho1kat.entity.Airport;
import com.sho1kat.entity.City;
import com.sho1kat.mapper.AirportMapper;
import com.sho1kat.payload.request.AirportRequest;
import com.sho1kat.payload.response.AirportResponse;
import com.sho1kat.repository.AirportRepository;
import com.sho1kat.repository.CityRepository;
import com.sho1kat.service.AirportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AirportServiceImpl implements AirportService {

    private final AirportRepository airportRepository;
    private final CityRepository cityRepository;
    private final AirportMapper airportMapper;

    @Override
    public AirportResponse createAirport(
            AirportRequest airportRequest
    ) {

        String iataCode = airportRequest.getIataCode().toUpperCase();

        if (airportRepository.existsByIataCodeIgnoreCase(iataCode)) {
            throw new IllegalArgumentException(
                    "Airport with IATA code " + iataCode + " already exists"
            );
        }

        City city = cityRepository.findById(airportRequest.getCityId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "City not found with id: "
                                        + airportRequest.getCityId()
                        )
                );

        Airport airport = airportMapper.toEntity(airportRequest);
        airport.setCity(city);

        Airport savedAirport = airportRepository.save(airport);

        clearAirportCaches();

        return airportMapper.toResponse(savedAirport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirportResponse> getAllAirports(
            Pageable pageable
    ) {

        return airportRepository.findAll(pageable)
                .map(airportMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "airports",
            key = "#id"
    )
    public AirportResponse getAirportById(Long id) {

        Airport airport = airportRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Airport not found with id: " + id
                        )
                );

        return airportMapper.toResponse(airport);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "airportsByIata",
            key = "#iataCode.toUpperCase()"
    )
    public AirportResponse getAirportByIataCode(
            String iataCode
    ) {

        Airport airport =
                airportRepository.findByIataCodeIgnoreCase(iataCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Airport not found with IATA code: "
                                                + iataCode
                                )
                        );

        return airportMapper.toResponse(airport);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "airportsByCity",
            key = "#cityId"
    )
    public List<AirportResponse> getAirportsByCityId(
            Long cityId
    ) {

        if (!cityRepository.existsById(cityId)) {
            throw new IllegalArgumentException(
                    "City not found with id: " + cityId
            );
        }

        return airportRepository.findAllByCityId(cityId)
                .stream()
                .map(airportMapper::toResponse)
                .toList();
    }

    @Override
    public AirportResponse updateAirport(
            Long id,
            AirportRequest airportRequest
    ) {

        Airport airport = airportRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Airport not found with id: " + id
                        )
                );

        String iataCode =
                airportRequest.getIataCode().toUpperCase();

        if (airportRepository.existsByIataCodeIgnoreCaseAndIdNot(
                iataCode,
                id
        )) {
            throw new IllegalArgumentException(
                    "Airport with IATA code "
                            + iataCode
                            + " already exists"
            );
        }

        City city = cityRepository.findById(
                        airportRequest.getCityId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "City not found with id: "
                                        + airportRequest.getCityId()
                        )
                );

        airportMapper.updateEntity(
                airport,
                airportRequest
        );

        airport.setCity(city);

        Airport updatedAirport =
                airportRepository.save(airport);

        clearAirportCaches();

        return airportMapper.toResponse(updatedAirport);
    }

    @Override
    public void deleteAirportById(Long id) {

        if (!airportRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Airport not found with id: " + id
            );
        }

        airportRepository.deleteById(id);

        clearAirportCaches();
    }

    @CacheEvict(
            value = {
                    "airports",
                    "airportsByIata",
                    "airportsByCity"
            },
            allEntries = true
    )
    protected void clearAirportCaches() {
        // Cache eviction only
    }
}