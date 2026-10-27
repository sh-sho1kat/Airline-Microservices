package com.sho1kat.service.impl;

import com.sho1kat.entity.BaggagePolicy;
import com.sho1kat.enums.CabinClass;
import com.sho1kat.mapper.BaggagePolicyMapper;
import com.sho1kat.payload.pricingServiceDto.request.BaggagePolicyRequest;
import com.sho1kat.payload.pricingServiceDto.response.BaggagePolicyResponse;
import com.sho1kat.repository.BaggagePolicyRepository;
import com.sho1kat.service.BaggagePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BaggagePolicyServiceImpl implements BaggagePolicyService {

    private final BaggagePolicyRepository baggagePolicyRepository;
    private final BaggagePolicyMapper baggagePolicyMapper;

    // Create a baggage policy
    @Override
    @Transactional
    public BaggagePolicyResponse createBaggagePolicy(BaggagePolicyRequest baggagePolicyRequest) {

        if (baggagePolicyRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Baggage policy request is required"
            );
        }

        if (baggagePolicyRepository.findByAirlineIdAndCabin(
                baggagePolicyRequest.getAirlineId(),
                baggagePolicyRequest.getCabin()
        ).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A baggage policy already exists for this airline and cabin; "
                            + "update/reactivate it instead"
            );
        }

        BaggagePolicy policy = baggagePolicyMapper.toEntity(baggagePolicyRequest);

        return baggagePolicyMapper.toResponse(baggagePolicyRepository.save(policy));
    }

    // Public airline/cabin lookup of the active policy
    @Override
    public BaggagePolicyResponse findActiveForAirlineAndCabin(Long airlineId, CabinClass cabin) {

        BaggagePolicy policy = baggagePolicyRepository.findByAirlineIdAndCabinAndIsActiveTrue(airlineId, cabin)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No active baggage policy found for this airline and cabin"
                ));

        return baggagePolicyMapper.toResponse(policy);
    }

    // Get all baggage policies with an optional airline filter
    @Override
    public List<BaggagePolicyResponse> getAllBaggagePolicies(Long airlineId) {

        if (airlineId != null && airlineId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Airline ID must be a positive number"
            );
        }

        return (airlineId == null
                ? baggagePolicyRepository.findAll()
                : baggagePolicyRepository.findAllByAirlineId(airlineId))
                .stream()
                .map(baggagePolicyMapper::toResponse)
                .toList();
    }

    // Update a baggage policy
    @Override
    @Transactional
    public BaggagePolicyResponse updateBaggagePolicy(Long id, BaggagePolicyRequest baggagePolicyRequest) {

        if (baggagePolicyRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Baggage policy request is required"
            );
        }

        BaggagePolicy existing = findBaggagePolicy(id);

        baggagePolicyRepository.findByAirlineIdAndCabin(
                        baggagePolicyRequest.getAirlineId(),
                        baggagePolicyRequest.getCabin()
                )
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A baggage policy already exists for this airline and cabin"
                    );
                });

        baggagePolicyMapper.updateEntity(baggagePolicyRequest, existing);

        return baggagePolicyMapper.toResponse(baggagePolicyRepository.save(existing));
    }

    // Deactivate a baggage policy (soft delete)
    @Override
    @Transactional
    public void deactivateBaggagePolicy(Long id) {

        BaggagePolicy existing = findBaggagePolicy(id);

        existing.setActive(false);

        baggagePolicyRepository.save(existing);
    }

    private BaggagePolicy findBaggagePolicy(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Baggage policy ID must be a positive number"
            );
        }

        return baggagePolicyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Baggage policy not found with ID: " + id
                ));
    }
}
