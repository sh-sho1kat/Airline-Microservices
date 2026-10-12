package com.sho1kat.service.impl;

import com.sho1kat.entity.Aircraft;
import com.sho1kat.entity.Airline;
import com.sho1kat.enums.AircraftStatus;
import com.sho1kat.enums.AirlineStatus;
import com.sho1kat.exception.ApiException;
import com.sho1kat.mapper.AircraftMapper;
import com.sho1kat.payload.airlineServiceDto.request.AircraftRequest;
import com.sho1kat.payload.airlineServiceDto.response.AircraftResponse;
import com.sho1kat.payload.airlineServiceDto.response.AircraftStatusResponse;
import com.sho1kat.repository.AircraftRepository;
import com.sho1kat.repository.AirlineRepository;
import com.sho1kat.service.AircraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AircraftServiceImpl implements AircraftService {

    private final AirlineRepository airlineRepository;
    private final AircraftRepository aircraftRepository;

    @Override
    @Transactional
    public AircraftResponse createAircraft(AircraftRequest request, Long callerId) {
        if (aircraftRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "An aircraft with this code already exists.");
        }
        Airline airline = airlineRepository.findById(request.getAirlineId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Airline not found."));
        if(airline.getStatus() != AirlineStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot add aircraft to an inactive airline.");
        }
        Aircraft aircraft = AircraftMapper.toEntity(request, airline, callerId);
        Aircraft savedAircraft = aircraftRepository.save(aircraft);
        return AircraftMapper.toResponse(savedAircraft);
    }

    @Override
    @Transactional(readOnly = true)
    public AircraftResponse getById(Long id) {
        return aircraftRepository.findById(id)
                .map(AircraftMapper::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aircraft not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public AircraftStatusResponse getAircraftStatus(Long id) {
        return aircraftRepository.findById(id)
                .map(AircraftMapper::toStatusResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aircraft not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AircraftResponse> listAircraft(AircraftStatus status, Pageable pageable) {
        if (status == null) {
            return null;
        }
        return aircraftRepository.findByStatusOrderByCodeAsc(status, pageable)
                    .map(AircraftMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AircraftResponse> listAircraftByAirline(Long airlineId, Pageable pageable) {
        return aircraftRepository.findByAirline_Id(airlineId, pageable)
                .map(AircraftMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AircraftResponse> listAircraftByAirlineAndStatus(Long airlineId, AircraftStatus status, Pageable pageable) {
        return aircraftRepository.findByAirline_IdAndStatus(airlineId, status, pageable)
                .map(AircraftMapper::toResponse);
    }

    @Override
    @Transactional
    public AircraftResponse updateAircraft(Long id, AircraftRequest request, Long callerId) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aircraft not found."));
        Airline airline = airlineRepository.findById(request.getAirlineId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Airline not found."));
        if(airline.getStatus() != AirlineStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot assign aircraft to an inactive airline.");
        }
        if (aircraftRepository.existsByCodeIgnoreCaseAndIdNot(request.getCode().trim(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "An aircraft with this code already exists.");
        }
        AircraftMapper.applyUpdate(aircraft, request, airline, callerId);
        Aircraft updatedAircraft = aircraftRepository.save(aircraft);
        return AircraftMapper.toResponse(updatedAircraft);
    }

    @Override
    @Transactional
    public AircraftStatusResponse updateAircraftStatus(Long id, AircraftStatus status, Long callerId) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aircraft not found."));
        aircraft.setStatus(status);
        aircraft.setUpdatedById(callerId);
        Aircraft updatedAircraft = aircraftRepository.save(aircraft);
        return AircraftMapper.toStatusResponse(updatedAircraft);
    }

    @Override
    @Transactional
    public void deleteAircraft(Long id, Long callerId) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aircraft not found."));
        aircraft.setUpdatedById(callerId);
        aircraftRepository.deleteById(id);
    }
}
