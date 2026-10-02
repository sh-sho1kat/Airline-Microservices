package com.sho1kat.mapper;

import com.sho1kat.embeddable.Address;
import com.sho1kat.embeddable.GeoCode;
import com.sho1kat.entity.Airport;
import com.sho1kat.payload.request.AddressRequest;
import com.sho1kat.payload.request.AirportRequest;
import com.sho1kat.payload.request.GeoCodeRequest;
import com.sho1kat.payload.response.AddressResponse;
import com.sho1kat.payload.response.AirportResponse;
import com.sho1kat.payload.response.GeoCodeResponse;
import org.springframework.stereotype.Component;

@Component
public class AirportMapper {

    public Airport toEntity(AirportRequest request) {

        return Airport.builder()
                .iataCode(request.getIataCode().toUpperCase())
                .name(request.getName())
                .timeZoneId(request.getTimeZoneId())
                .address(toAddressEntity(request.getAddress()))
                .geoCode(toGeoCodeEntity(request.getGeoCode()))
                .build();
    }

    public void updateEntity(
            Airport airport,
            AirportRequest request
    ) {

        airport.setIataCode(request.getIataCode().toUpperCase());
        airport.setName(request.getName());
        airport.setTimeZoneId(request.getTimeZoneId());
        airport.setAddress(toAddressEntity(request.getAddress()));
        airport.setGeoCode(toGeoCodeEntity(request.getGeoCode()));
    }

    public AirportResponse toResponse(Airport airport) {

        return AirportResponse.builder()
                .id(airport.getId())
                .iataCode(airport.getIataCode())
                .name(airport.getName())
                .timeZoneId(airport.getTimeZoneId())
                .address(toAddressResponse(airport.getAddress()))
                .geoCode(toGeoCodeResponse(airport.getGeoCode()))
                .build();
    }

    private Address toAddressEntity(AddressRequest request) {

        if (request == null) {
            return null;
        }

        return Address.builder()
                .addressLine(request.getAddressLine())
                .postalCode(request.getPostalCode())
                .district(request.getDistrict())
                .country(request.getCountry())
                .build();
    }

    private GeoCode toGeoCodeEntity(GeoCodeRequest request) {

        if (request == null) {
            return null;
        }

        return GeoCode.builder()
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    private AddressResponse toAddressResponse(Address address) {

        if (address == null) {
            return null;
        }

        return AddressResponse.builder()
                .addressLine(address.getAddressLine())
                .postalCode(address.getPostalCode())
                .district(address.getDistrict())
                .country(address.getCountry())
                .build();
    }

    private GeoCodeResponse toGeoCodeResponse(GeoCode geoCode) {

        if (geoCode == null) {
            return null;
        }

        return GeoCodeResponse.builder()
                .latitude(geoCode.getLatitude())
                .longitude(geoCode.getLongitude())
                .build();
    }
}