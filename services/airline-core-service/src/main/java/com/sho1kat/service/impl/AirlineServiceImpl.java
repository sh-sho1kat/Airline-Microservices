package com.sho1kat.service.impl;

import com.sho1kat.entity.Airline;
import com.sho1kat.enums.AirlineStatus;
import com.sho1kat.exception.ApiException;
import com.sho1kat.mapper.AirlineMapper;
import com.sho1kat.payload.airlineServiceDto.request.AirlineRequest;
import com.sho1kat.payload.airlineServiceDto.response.AirlineResponse;
import com.sho1kat.payload.dto.AirlineDropdownItem;
import com.sho1kat.repository.AirlineRepository;
import com.sho1kat.service.AirlineService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class AirlineServiceImpl implements AirlineService {
    private final AirlineRepository airlineRepository;
    @Override
    public AirlineResponse createAirline(AirlineRequest request, Long callerId) {
        String iataCode = request.getIataCode().trim();
        String icaoCode = request.getIcaoCode().trim();
        if (airlineRepository.existsByIataCodeIgnoreCase(iataCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "An airline with this IATA code already exists.");
        }
        if (airlineRepository.existsByIcaoCodeIgnoreCase(icaoCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "An airline with this ICAO code already exists.");
        }
        Airline airline = AirlineMapper.toEntity(request, callerId);
        Airline savedAirline = airlineRepository.save(airline);
        return AirlineMapper.toResponse(savedAirline);
    }

    @Override
    public AirlineResponse getAirlineByOwner(Long ownerId) {
        Airline airline = airlineRepository.findFirstByOwnerIdOrderByCreatedAtDesc(ownerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No airline found for the given owner ID." + ownerId));
        return AirlineMapper.toResponse(airline);
    }

    @Override
    public AirlineResponse getAirlineById(Long id) {
        Airline airline = airlineRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No airline found for the given ID." + id));
        return AirlineMapper.toResponse(airline);
    }

    @Override
    public AirlineResponse getAirlineByIataCode(String code) {
        Airline airline = airlineRepository.findByIataCodeIgnoreCase(code)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No airline found for the given IATA code." + code));
        return AirlineMapper.toResponse(airline);
    }

    @Override
    public Page<AirlineResponse> getAllAirlines(Pageable pageable) {
        return airlineRepository.findAll(pageable).map(AirlineMapper::toResponse);
    }

    @Override
    public Page<AirlineResponse> getAllAirlinesByStatus(AirlineStatus status, Pageable pageable) {
        return airlineRepository.findByStatusOrderByNameAsc(status, pageable).map(AirlineMapper::toResponse);
    }

    @Override
    public AirlineResponse updateAirline(Long id, AirlineRequest request, Long callerId) {
        Airline airline = airlineRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No airline found for the given ID." + id));

        String newIataCode = request.getIataCode().trim();
        String newIcaoCode = request.getIcaoCode().trim();
        if (!airline.getIataCode().equalsIgnoreCase(newIataCode) && airlineRepository.existsByIataCodeIgnoreCase(newIataCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "An airline with this IATA code already exists.");
        }
        if (!airline.getIcaoCode().equalsIgnoreCase(newIcaoCode) && airlineRepository.existsByIcaoCodeIgnoreCase(newIcaoCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "An airline with this ICAO code already exists.");
        }


        AirlineMapper.applyUpdate(airline, request, callerId);
        Airline savedAirline = airlineRepository.save(airline);
        return AirlineMapper.toResponse(savedAirline);
    }

    @Override
    public void deleteAirline(Long id, Long ownerId) {
        Airline airline = airlineRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No airline found for the given ID." + id));
        airlineRepository.delete(airline);
    }

    @Override
    public AirlineResponse changeStatusByAdmin(Long airlineId, AirlineStatus status, Long callerId) {
        Airline airline = airlineRepository.findById(airlineId)
              .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No airline found for the given ID." + airlineId));
        AirlineStatus currentStatus = airline.getStatus();
        if(currentStatus == AirlineStatus.PENDING && status == AirlineStatus.INACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "An airline cannot be moved directly from PENDING to INACTIVE");
        }
        if(status.equals(currentStatus)) {
            throw new ApiException(HttpStatus.CONFLICT, "Airline " + airlineId + " is already " + currentStatus);
        }
        if(status.equals(AirlineStatus.PENDING)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "An airline cannot be moved back to PENDING");
        }
        airline.setStatus(status);
        airline.setUpdatedById(callerId);
        Airline savedAirline = airlineRepository.save(airline);
        return AirlineMapper.toResponse(savedAirline);
    }

    @Override
    public List<AirlineDropdownItem> getAirlineDropdown() {
        return airlineRepository.findByStatus(AirlineStatus.ACTIVE)
                .stream()
                .map(AirlineMapper::toResponse)
                .map(airline -> AirlineDropdownItem.builder()
                        .id(airline.getId())
                        .iataCode(airline.getIataCode())
                        .icaoCode(airline.getIcaoCode())
                        .name(airline.getName())
                        .build()
                ).toList();
    }
}
