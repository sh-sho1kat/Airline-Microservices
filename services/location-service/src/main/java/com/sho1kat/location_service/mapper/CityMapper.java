package com.sho1kat.location_service.mapper;

import com.sho1kat.location_service.entity.City;
import com.sho1kat.location_service.payload.request.CityRequest;
import com.sho1kat.location_service.payload.response.CityResponse;

public class CityMapper {

    public static City toEntity(CityRequest cityRequest) {
        if (cityRequest == null) {
            return null;
        }
        return City.builder()
                .cityName(normalizeText(cityRequest.getCityName()))
                .cityCode(normalizeCode(cityRequest.getCityCode()))
                .countryName(normalizeText(cityRequest.getCountryName()))
                .countryCode(normalizeCode(cityRequest.getCountryCode()))
                .regionCode(normalizeCode(cityRequest.getRegionCode()))
                .timeZoneId(normalizeText(cityRequest.getTimeZoneId()))
                .build();
    }

    public static CityResponse toResponse(City city) {

        if (city == null) {
            return null;
        }
        return CityResponse.builder()
                .cityName(city.getCityName())
                .cityCode(city.getCityCode())
                .countryCode(city.getCountryCode())
                .countryName(city.getCountryName())
                .regionCode(city.getRegionCode())
                .timeZoneId(city.getTimeZoneId())
                .build();
    }

    public static City toUpdateEntity(City city, CityRequest cityRequest) {
        if (city == null || cityRequest == null) {
            return city;
        }
        if (cityRequest.getCityName() != null) {
            city.setCityName(
                    cityRequest.getCityName().trim()
            );
        }

        if (cityRequest.getCityCode() != null) {
            city.setCityCode(
                    cityRequest.getCityCode()
                            .trim()
                            .toUpperCase()
            );
        }

        if (cityRequest.getCountryCode() != null) {
            city.setCountryCode(
                    cityRequest.getCountryCode()
                            .trim()
                            .toUpperCase()
            );
        }

        if (cityRequest.getCountryName() != null) {
            city.setCountryName(
                    cityRequest.getCountryName().trim()
            );
        }

        if (cityRequest.getRegionCode() != null) {
            city.setRegionCode(
                    cityRequest.getRegionCode()
                            .trim()
                            .toUpperCase()
            );
        }

        if (cityRequest.getTimeZoneId() != null) {
            city.setTimeZoneId(
                    cityRequest.getTimeZoneId().trim()
            );
        }

        return city;
    }

    private static String normalizeText(String value) {

        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private static String normalizeCode(String value) {

        if (value == null) {
            return null;
        }

        return value.trim().toUpperCase();
    }
}