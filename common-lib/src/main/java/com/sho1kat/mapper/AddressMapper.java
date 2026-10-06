package com.sho1kat.mapper;

import com.sho1kat.embeddable.Address;
import com.sho1kat.payload.response.AddressResponse;

public class AddressMapper {

    private AddressMapper() {
    }

    public static Address toEntity(AddressResponse response) {
        if (response == null) {
            return null;
        }

        return Address.builder()
                .addressLine(response.getAddressLine())
                .postalCode(response.getPostalCode())
                .district(response.getDistrict())
                .country(response.getCountry())
                .build();
    }

    public static AddressResponse toResponse(Address address) {
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
}