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
            return aircraftRepository.findAll(pageable)
                    .map(AircraftMapper::toResponse);
        }
        return aircraftRepository.findByStatus(status, pageable)
                    .map(AircraftMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AircraftResponse> listAircraftByAirline(Long airlineId, Pageable pageable) {
        Page<Aircraft> page = (airlineId == null)
                ? aircraftRepository.findAll(pageable)
                : aircraftRepository.findByAirline_Id(airlineId, pageable);
        return page.map(AircraftMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AircraftResponse> listAircraftByAirlineAndStatus(Long airlineId, AircraftStatus status, Pageable pageable) {
        Page<Aircraft> page;
        if (airlineId != null && status != null) page = aircraftRepository.findByAirline_IdAndStatus(airlineId, status, pageable);
        else if (airlineId != null)              page = aircraftRepository.findByAirline_Id(airlineId, pageable);
        else if (status != null)                 page = aircraftRepository.findByStatus(status, pageable);
        else                                     page = aircraftRepository.findAll(pageable);
        return page.map(AircraftMapper::toResponse);
    }

    @Override
    @Transactional
    public AircraftResponse updateAircraft(Long id, AircraftRequest request, Long callerId) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aircraft not found."));
        Airline airline = airlineRepository.findById(request.getAirlineId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Airline not found."));
        boolean airlineChanged = !aircraft.getAirline().getId().equals(airline.getId());
        if (airlineChanged && airline.getStatus() != AirlineStatus.ACTIVE) {
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
        if (aircraft.getStatus() == status) {
            throw new ApiException(HttpStatus.CONFLICT, "Aircraft is already " + status + ".");
        }
        if (aircraft.getStatus() == AircraftStatus.RETIRED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A retired aircraft cannot change status.");
        }
        if (status == AircraftStatus.ACTIVE && aircraft.getAirline().getStatus() != AirlineStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot activate an aircraft of a non-active airline.");
        }
        aircraft.setStatus(status);
        if (status != AircraftStatus.ACTIVE) {
            aircraft.setIsAvailable(false);
        }
        aircraft.setUpdatedById(callerId);
        Aircraft updatedAircraft = aircraftRepository.save(aircraft);
        return AircraftMapper.toStatusResponse(updatedAircraft);
    }

    @Override
    @Transactional
    public void deleteAircraft(Long id, Long callerId) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aircraft not found."));
        if (aircraftRepository.existsByAirline_Id(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete an airline that still has aircraft.");
        }
        aircraft.setUpdatedById(callerId);
        aircraftRepository.deleteById(id);
    }
}
