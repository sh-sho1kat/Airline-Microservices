package com.sho1kat.service;

import com.sho1kat.enums.AircraftStatus;
import com.sho1kat.payload.airlineServiceDto.request.AircraftRequest;
import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AircraftStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AircraftService {

    AircraftResponse createAircraft(AircraftRequest request, Long callerId);
    AircraftResponse getById(Long id);
    AircraftStatusResponse getAircraftStatus(Long id);
    Page<AircraftResponse> listAircraft(AircraftStatus status, Pageable pageable);
    Page<AircraftResponse> listAircraftByAirline(Long airlineId, Pageable pageable);
    Page<AircraftResponse> listAircraftByAirlineAndStatus(Long airlineId, AircraftStatus status, Pageable pageable);
    AircraftResponse updateAircraft(Long id, AircraftRequest request, Long callerId);
    AircraftStatusResponse updateAircraftStatus(Long id, AircraftStatus status, Long callerId);
    void deleteAircraft(Long id, Long callerId);
}