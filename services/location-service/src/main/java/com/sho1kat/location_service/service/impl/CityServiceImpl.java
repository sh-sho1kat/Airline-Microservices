package com.sho1kat.location_service.service.impl;

import com.sho1kat.location_service.entity.City;
import com.sho1kat.location_service.mapper.CityMapper;
import com.sho1kat.location_service.payload.request.CityRequest;
import com.sho1kat.location_service.payload.response.CityResponse;
import com.sho1kat.location_service.repository.CityRepository;
import com.sho1kat.location_service.service.CityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;


    // CREATE
    @Override
    @Transactional
    public CityResponse createCity(CityRequest cityRequest) {

        if (cityRequest == null) {
            throw new IllegalArgumentException(
                    "cityRequest cannot be null"
            );
        }

        String cityCode = cityRequest.getCityCode();

        if (cityExists(cityCode)) {
            throw new IllegalArgumentException(
                    "City with given code already exists"
            );
        }

        City city = CityMapper.toEntity(cityRequest);

        City savedCity = cityRepository.save(city);

        return CityMapper.toResponse(savedCity);
    }


    // GET BY ID
    @Override
    public CityResponse getCityById(Long id) {

        City city = cityRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "City not found with id: " + id
                        )
                );

        return CityMapper.toResponse(city);
    }


    // UPDATE
    @Override
    @Transactional
    public CityResponse updateCity(Long id, CityRequest cityRequest) {

        if (cityRequest == null) {
            throw new IllegalArgumentException(
                    "cityRequest cannot be null"
            );
        }

        City city = cityRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "City not found with id: " + id
                        )
                );

        String cityCode = cityRequest.getCityCode();

        if (cityCode != null) {
            cityCode = cityCode.trim().toUpperCase();

            if (cityRepository.existsByCityCodeAndIdNot(cityCode, id)) {
                throw new IllegalArgumentException(
                        "Another city with the given code already exists"
                );
            }
        }

        CityMapper.toUpdateEntity(city, cityRequest);
        City updatedCity = cityRepository.save(city);
        return CityMapper.toResponse(updatedCity);
    }


    // DELETE
    @Override
    @Transactional
    public void deleteCityById(Long id) {

        if (!cityRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "City not found with id: " + id
            );
        }
        cityRepository.deleteById(id);
    }


    // BULK CREATE
    @Override
    @Transactional
    public Page<CityResponse> createCityInGroup(
            List<CityRequest> cityRequests
    ) {

        if (cityRequests == null || cityRequests.isEmpty()) {
            throw new IllegalArgumentException(
                    "cityRequests cannot be null or empty"
            );
        }

        List<CityResponse> responses = cityRequests.stream()
                .map(this::createCity)
                .toList();

        return new PageImpl<>(responses);
    }


    // GET ALL
    @Override
    public Page<CityResponse> getAllCities(Pageable pageable) {
        return cityRepository
                .findAll(pageable)
                .map(CityMapper::toResponse);
    }


    // SEARCH
    @Override
    public Page<CityResponse> searchCities(String keyword, Pageable pageable) {

        if (keyword == null || keyword.isBlank()) {
            return getAllCities(pageable);
        }

        return cityRepository
                .searchByKeyWordIgnoreCase(keyword.trim(), pageable)
                .map(CityMapper::toResponse);
    }


    // GET BY COUNTRY CODE
    @Override
    public Page<CityResponse> getCitiesByCountryCode(String countryCode, Pageable pageable) {
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException(
                    "countryCode cannot be null or empty"
            );
        }

        return cityRepository
                .findByCountryCodeIgnoreCase(countryCode.trim(), pageable)
                .map(CityMapper::toResponse);
    }


    // CHECK EXISTS
    @Override
    public boolean cityExists(String cityCode) {

        if (cityCode == null || cityCode.isBlank()) {
            return false;
        }

        return cityRepository.existsByCityCode(
                cityCode.trim().toUpperCase()
        );
    }
}