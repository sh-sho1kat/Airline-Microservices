package com.sho1kat.service.impl;

import com.sho1kat.entity.FlightInstanceReference;
import com.sho1kat.mapper.FlightInstanceReferenceMapper;
import com.sho1kat.payload.pricingServiceDto.request.FlightInstanceSyncRequest;
import com.sho1kat.payload.pricingServiceDto.response.FlightInstanceSyncResponse;
import com.sho1kat.repository.FlightInstanceReferenceRepository;
import com.sho1kat.service.FareTemplateService;
import com.sho1kat.service.FlightInstanceReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FlightInstanceReferenceServiceImpl implements FlightInstanceReferenceService {

    private final FlightInstanceReferenceRepository flightInstanceReferenceRepository;
    private final FlightInstanceReferenceMapper flightInstanceReferenceMapper;
    private final FareTemplateService fareTemplateService;

    // Upsert the read-model row published by Flight Ops
    @Override
    @Transactional
    public FlightInstanceSyncResponse upsertFromFlightOps(FlightInstanceSyncRequest syncRequest) {

        if (syncRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight instance sync request is required"
            );
        }

        FlightInstanceReference existing = flightInstanceReferenceRepository
                .findById(syncRequest.getFlightInstanceId())
                .orElse(null);

        if (existing == null) {

            FlightInstanceReference reference = flightInstanceReferenceMapper.toEntity(syncRequest);

            flightInstanceReferenceRepository.save(reference);

            /*
             * Only the first registration gets priced from templates; retries and later
             * updates must never silently re-price existing fares.
             */
            int templatesApplied = fareTemplateService.applyTemplatesToInstance(
                    reference.getFlightInstanceId(),
                    reference.getFlightId(),
                    reference.getAirlineId(),
                    reference.getDepartureAt()
            );

            return FlightInstanceSyncResponse.builder()
                    .flightInstanceId(reference.getFlightInstanceId())
                    .fareTemplatesApplied(templatesApplied)
                    .referenceCreated(true)
                    .build();
        }

        flightInstanceReferenceMapper.updateEntity(syncRequest, existing);

        flightInstanceReferenceRepository.save(existing);

        return FlightInstanceSyncResponse.builder()
                .flightInstanceId(existing.getFlightInstanceId())
                .fareTemplatesApplied(0)
                .referenceCreated(false)
                .build();
    }
}
