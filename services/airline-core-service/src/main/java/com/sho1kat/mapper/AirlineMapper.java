package com.sho1kat.mapper;

import com.sho1kat.embeddable.Support;
import com.sho1kat.entity.Airline;
import com.sho1kat.enums.AirlineStatus;
import com.sho1kat.payload.airlineServiceDto.request.AirlineRequest;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.dto.AirlineDropdownItem;
import org.springframework.util.StringUtils;

public final class AirlineMapper {
    private AirlineMapper() {
    }

    public static Airline toEntity(AirlineRequest request, Long updatedById) {
        if(request == null) return null;
        Airline airline = Airline.builder()
                .iataCode(normalizeCode(request.getIataCode()))
                .icaoCode(normalizeCode(request.getIcaoCode()))
                .name(request.getName().trim())
                .alias(trimToNull(request.getAlias()))
                .country(request.getCountry().trim())
                .logoUrl(trimToNull(request.getLogoUrl()))
                .website(trimToNull(request.getWebsite()))
                .alliance(trimToNull(request.getAlliance()))
                .headquartersCityId(request.getHeadquartersCityId())
                .ownerId(request.getOwnerId())
                .updatedById(updatedById)
                .build();
        airline.setSupport(toSupport(request));
        return airline;
    }

    public static void applyUpdate(Airline airline, AirlineRequest request, Long updatedById) {
        airline.setIataCode(normalizeCode(request.getIataCode()));
        airline.setIcaoCode(normalizeCode(request.getIcaoCode()));
        airline.setName(request.getName().trim());
        airline.setAlias(trimToNull(request.getAlias()));
        airline.setCountry(request.getCountry().trim());
        airline.setLogoUrl(trimToNull(request.getLogoUrl()));
        airline.setWebsite(trimToNull(request.getWebsite()));
        airline.setAlliance(trimToNull(request.getAlliance()));
        airline.setHeadquartersCityId(request.getHeadquartersCityId());
        airline.setSupport(toSupport(request));
        airline.setUpdatedById(updatedById);
    }

    public static AirlineResponse toResponse(Airline airline) {
        if (airline == null) return null;
        return AirlineResponse.builder()
                .id(airline.getId())
                .iataCode(airline.getIataCode())
                .icaoCode(airline.getIcaoCode())
                .name(airline.getName())
                .alias(airline.getAlias())
                .country(airline.getCountry())
                .logoUrl(airline.getLogoUrl())
                .website(airline.getWebsite())
                .status(airline.getStatus())
                .alliance(airline.getAlliance())
                .headquartersCityId(airline.getHeadquartersCityId())
                .support(airline.getSupport())
                .createdAt(airline.getCreatedAt())
                .updatedAt(airline.getUpdatedAt())
                .ownerId(airline.getOwnerId())
                .updatedById(airline.getUpdatedById())
                .build();
    }

    public static AirlineDropdownItem toDropdownItem(Airline airline) {
        return AirlineDropdownItem.builder()
                .id(airline.getId())
                .name(airline.getName())
                .iataCode(airline.getIataCode())
                .icaoCode(airline.getIcaoCode())
                .logoUrl(airline.getLogoUrl())
                .country(airline.getCountry())
                .build();
    }

    private static Support toSupport(AirlineRequest request) {
        if (!StringUtils.hasText(request.getSupportEmail())
                && !StringUtils.hasText(request.getSupportPhone())
                && !StringUtils.hasText(request.getSupportHours())) {
            return null;
        }
        return Support.builder()
                .email(trimToNull(request.getSupportEmail()))
                .phone(trimToNull(request.getSupportPhone()))
                .hours(trimToNull(request.getSupportHours()))
                .build();
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
