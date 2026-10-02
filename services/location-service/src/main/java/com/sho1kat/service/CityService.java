package com.sho1kat.service;

import com.sho1kat.payload.request.CityRequest;
import com.sho1kat.payload.response.CityResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CityService {

    CityResponse createCity(CityRequest cityRequest);
    CityResponse getCityById(Long id);
    CityResponse updateCity(Long id, CityRequest cityRequest);

    void deleteCityById(Long id);

    Page<CityResponse> createCityInGroup(List<CityRequest> cityRequests);
    Page<CityResponse> getAllCities(Pageable pageable);

    Page<CityResponse> searchCities(String keyword, Pageable pageable);

    Page<CityResponse> getCitiesByCountryCode(String countryCode, Pageable pageable);

    boolean cityExists(String cityCode);
}