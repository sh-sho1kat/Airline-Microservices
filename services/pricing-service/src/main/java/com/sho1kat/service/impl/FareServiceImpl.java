package com.sho1kat.service.impl;

import com.sho1kat.entity.Fare;
import com.sho1kat.entity.FareRule;
import com.sho1kat.entity.FareTemplate;
import com.sho1kat.enums.CabinClass;
import com.sho1kat.mapper.FareMapper;
import com.sho1kat.mapper.FlightInstanceReferenceMapper;
import com.sho1kat.payload.pricingServiceDto.request.FareRequest;
import com.sho1kat.payload.pricingServiceDto.request.LowestFareRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareResponse;
import com.sho1kat.payload.pricingServiceDto.response.LowestFareResponse;
import com.sho1kat.payload.pricingServiceDto.response.UnpricedFlightResponse;
import com.sho1kat.repository.FareRepository;
import com.sho1kat.repository.FlightInstanceReferenceRepository;
import com.sho1kat.service.FareService;
import com.sho1kat.service.FareRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FareServiceImpl implements FareService {

    // Bulk fare creation maximum per request.
    private static final int MAX_BULK_SIZE = 200;

    // Cap on the unpriced-instance report.
    private static final int MAX_UNPRICED_RESULTS = 500;

    // Default look-ahead window for the unpriced report (in days).
    private static final int DEFAULT_UNPRICED_DAYS = 30;

    private static final Comparator<Fare> FARE_ORDER = Comparator
            .comparing((Fare fare) -> fare.getBasePrice().add(fare.getBasePrice().multiply(fare.getTaxRate())))
            .thenComparing(Fare::getFareClass);

    private final FareRepository fareRepository;
    private final FareRuleService fareRuleService;
    private final FlightInstanceReferenceRepository flightInstanceReferenceRepository;
    private final FlightInstanceReferenceMapper flightInstanceReferenceMapper;
    private final FareMapper fareMapper;

    // Create a fare
    @Override
    @Transactional
    public FareResponse createFare(FareRequest fareRequest) {

        if (fareRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare request is required"
            );
        }

        validateFareRequest(fareRequest);

        if (fareRepository.findByFlightInstanceIdAndCabinAndFareClass(
                fareRequest.getFlightInstanceId(),
                fareRequest.getCabin(),
                fareRequest.getFareClass()
        ).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A fare already exists for this flight instance, cabin and fare class; "
                            + "update/reactivate it instead"
            );
        }

        FareRule fareRule = fareRuleService.findActiveFareRule(fareRequest.getFareRuleId());
        Fare fare = fareMapper.toEntity(fareRequest, fareRule);

        try {
            return fareMapper.toResponse(fareRepository.save(fare));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A fare already exists for this flight instance, cabin and fare class"
            );
        }
    }

    // Create fares in bulk (1-200, all-or-nothing)
    @Override
    @Transactional
    public List<FareResponse> createFaresInBulk(List<FareRequest> fareRequests) {

        if (fareRequests == null || fareRequests.isEmpty() || fareRequests.size() > MAX_BULK_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bulk fare requests must contain between 1 and " + MAX_BULK_SIZE + " fares"
            );
        }

        Set<String> seenCombinations = new HashSet<>();

        for (FareRequest fareRequest : fareRequests) {

            if (fareRequest == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Bulk fare requests must not contain null fares"
                );
            }

            validateFareRequest(fareRequest);

            String combination = fareRequest.getFlightInstanceId()
                    + ":" + fareRequest.getCabin()
                    + ":" + fareRequest.getFareClass();

            if (!seenCombinations.add(combination)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Duplicate fare combination in bulk request: " + combination
                );
            }

            if (fareRepository.findByFlightInstanceIdAndCabinAndFareClass(
                    fareRequest.getFlightInstanceId(),
                    fareRequest.getCabin(),
                    fareRequest.getFareClass()
            ).isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Fare already exists for combination: " + combination
                );
            }
        }

        List<Fare> savedFares = new ArrayList<>();

        for (FareRequest fareRequest : fareRequests) {
            FareRule fareRule = fareRuleService.findActiveFareRule(fareRequest.getFareRuleId());
            savedFares.add(fareRepository.save(fareMapper.toEntity(fareRequest, fareRule)));
        }

        // Force constraint violations inside this transaction.
        fareRepository.flush();

        return savedFares.stream()
                .map(fareMapper::toResponse)
                .toList();
    }

    // Get current active fares for an instance
    @Override
    public List<FareResponse> getFaresByFlightInstanceId(Long flightInstanceId) {

        validateId(flightInstanceId, "Flight instance ID");

        LocalDateTime now = LocalDateTime.now();

        return fareRepository.findAllByFlightInstanceIdAndIsActiveTrue(flightInstanceId).stream()
                .filter(fare -> isFareCurrentlyValid(fare, now))
                .sorted(FARE_ORDER)
                .map(fareMapper::toResponse)
                .toList();
    }

    // Get a fare by ID
    @Override
    public FareResponse getFareById(Long fareId) {

        Fare fare = fareRepository.findById(fareId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fare not found with ID: " + fareId
                ));

        return fareMapper.toResponse(fare);
    }

    // Update a fare; flightInstanceId is immutable
    @Override
    @Transactional
    public FareResponse updateFare(Long fareId, FareRequest fareRequest) {

        if (fareRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare request is required"
            );
        }

        validateFareRequest(fareRequest);

        Fare existing = fareRepository.findById(fareId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fare not found with ID: " + fareId
                ));

        if (!existing.getFlightInstanceId().equals(fareRequest.getFlightInstanceId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "flightInstanceId cannot be changed after a fare is created"
            );
        }

        fareRepository.findByFlightInstanceIdAndCabinAndFareClass(
                        existing.getFlightInstanceId(),
                        fareRequest.getCabin(),
                        fareRequest.getFareClass()
                )
                .filter(other -> !other.getId().equals(fareId))
                .ifPresent(other -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Another fare already exists for this flight instance, cabin and fare class"
                    );
                });

        FareRule fareRule = fareRuleService.findActiveFareRule(fareRequest.getFareRuleId());

        fareMapper.updateEntity(fareRequest, existing, fareRule);

        return fareMapper.toResponse(fareRepository.save(existing));
    }

    // Deactivate a fare (soft delete)
    @Override
    @Transactional
    public void deactivateFare(Long fareId) {

        Fare existing = fareRepository.findById(fareId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fare not found with ID: " + fareId
                ));

        existing.setIsActive(false);

        fareRepository.save(existing);
    }

    // Lowest current fare for up to 200 instances and a cabin
    @Override
    public LowestFareResponse findLowestFares(LowestFareRequest lowestFareRequest) {

        if (lowestFareRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lowest fare request is required"
            );
        }

        List<Long> flightInstanceIds = lowestFareRequest.getFlightInstanceIds().stream()
                .distinct()
                .toList();

        CabinClass cabin = lowestFareRequest.getCabin();

        List<Fare> fares = fareRepository.findAllByFlightInstanceIdInAndCabinAndIsActiveTrue(
                flightInstanceIds, cabin
        );

        LocalDateTime now = LocalDateTime.now();

        List<LowestFareResponse.Price> prices = flightInstanceIds.stream()
                .map(instanceId -> fares.stream()
                        .filter(fare -> fare.getFlightInstanceId().equals(instanceId))
                        .filter(fare -> isFareCurrentlyValid(fare, now))
                        .min(FARE_ORDER)
                        .map(fare -> LowestFareResponse.Price.builder()
                                .flightInstanceId(instanceId)
                                .fareId(fare.getId())
                                .amount(fareMapper.calculateTotalPrice(fare))
                                .currency(fare.getCurrency())
                                .build())
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return LowestFareResponse.builder()
                .prices(prices)
                .build();
    }

    // Find synced active flight instances without any active fare
    @Override
    public List<UnpricedFlightResponse> findUnpricedInstances(Long flightId, LocalDate from, LocalDate to) {

        if (from != null && to != null && to.isBefore(from)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "to must be on or after from"
            );
        }

        // Defaults keep the query parameters bound; MySQL cannot store extreme date-times.
        if (from == null && to == null) {
            from = LocalDate.now();
        }

        if (from == null) {
            from = to.minusDays(DEFAULT_UNPRICED_DAYS);
        }

        if (to == null) {
            to = from.plusDays(DEFAULT_UNPRICED_DAYS);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        Pageable limit = PageRequest.of(0, MAX_UNPRICED_RESULTS);

        return flightInstanceReferenceRepository
                .findUnpricedInstances(flightId, start, end, now, limit)
                .stream()
                .map(flightInstanceReferenceMapper::toUnpricedResponse)
                .toList();
    }

    // Apply a template to a newly synced flight instance; null when a fare already exists
    @Override
    @Transactional
    public Fare applyTemplateToInstance(Long flightInstanceId, FareTemplate template) {

        if (fareRepository.findByFlightInstanceIdAndCabinAndFareClass(
                flightInstanceId, template.getCabin(), template.getFareClass()
        ).isPresent()) {
            return null;
        }

        Fare fare = fareMapper.toEntity(flightInstanceId, template);

        try {
            return fareRepository.save(fare);
        } catch (DataIntegrityViolationException ex) {
            // A concurrent template application won the race; treat the fare as existing.
            return null;
        }
    }

    // Whether the fare window covers the given time
    @Override
    public boolean isFareCurrentlyValid(Fare fare) {
        return isFareCurrentlyValid(fare, LocalDateTime.now());
    }

    private boolean isFareCurrentlyValid(Fare fare, LocalDateTime now) {
        return (fare.getValidFrom() == null || !fare.getValidFrom().isAfter(now))
                && (fare.getValidTo() == null || !fare.getValidTo().isBefore(now));
    }

    private void validateFareRequest(FareRequest request) {

        if (request.getValidFrom() != null && request.getValidTo() != null
                && !request.getValidTo().isAfter(request.getValidFrom())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "validTo must be after validFrom"
            );
        }
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be a positive number"
            );
        }
    }
}
