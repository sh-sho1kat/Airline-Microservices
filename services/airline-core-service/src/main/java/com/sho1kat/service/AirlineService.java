package com.sho1kat.service;

import com.sho1kat.enums.AirlineStatus;
import com.sho1kat.payload.airlineServiceDto.request.AirlineRequest;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.dto.AirlineDropdownItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AirlineService {
    AirlineResponse createAirline(AirlineRequest request, Long callerId);
    AirlineResponse getAirlineByOwner(Long ownerId);
    AirlineResponse getAirlineById(Long id);
    AirlineResponse getAirlineByIataCode(String code);
    Page<AirlineResponse> getAllAirlines(Pageable pageable);
    Page<AirlineResponse> getAllAirlinesByStatus(AirlineStatus status, Pageable pageable);
    AirlineResponse updateAirline(Long id, AirlineRequest request,Long callerId);
    void deleteAirline(Long id, Long ownerId);
    AirlineResponse changeStatusByAdmin(Long airlineId, AirlineStatus status, Long callerId);
    List<AirlineDropdownItem> getAirlineDropdown();
}
