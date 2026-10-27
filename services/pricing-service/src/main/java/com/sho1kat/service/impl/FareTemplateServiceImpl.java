package com.sho1kat.service.impl;

import com.sho1kat.entity.FareTemplate;
import com.sho1kat.mapper.FareTemplateMapper;
import com.sho1kat.payload.pricingServiceDto.request.FareTemplateRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareTemplateResponse;
import com.sho1kat.repository.FareTemplateRepository;
import com.sho1kat.service.FareRuleService;
import com.sho1kat.service.FareService;
import com.sho1kat.service.FareTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FareTemplateServiceImpl implements FareTemplateService {

    private final FareTemplateRepository fareTemplateRepository;
    private final FareRuleService fareRuleService;
    private final FareService fareService;
    private final FareTemplateMapper fareTemplateMapper;

    // Create a fare template
    @Override
    @Transactional
    public FareTemplateResponse createFareTemplate(FareTemplateRequest fareTemplateRequest) {

        if (fareTemplateRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare template request is required"
            );
        }

        validateTemplate(fareTemplateRequest);

        if (fareTemplateRepository.findByFlightIdAndCabinAndFareClass(
                fareTemplateRequest.getFlightId(),
                fareTemplateRequest.getCabin(),
                fareTemplateRequest.getFareClass()
        ).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A template already exists for this flight, cabin and fare class; "
                            + "update/reactivate it instead"
            );
        }

        FareTemplate template = fareTemplateMapper.toEntity(
                fareTemplateRequest,
                fareRuleService.findActiveFareRule(fareTemplateRequest.getFareRuleId())
        );

        return fareTemplateMapper.toResponse(fareTemplateRepository.save(template));
    }

    // Get all fare templates
    @Override
    public List<FareTemplateResponse> getAllFareTemplates() {
        return fareTemplateRepository.findAll().stream()
                .map(fareTemplateMapper::toResponse)
                .toList();
    }

    // Get a fare template by ID
    @Override
    public FareTemplateResponse getFareTemplateById(Long id) {
        return fareTemplateMapper.toResponse(findFareTemplate(id));
    }

    // Update a fare template
    @Override
    @Transactional
    public FareTemplateResponse updateFareTemplate(Long id, FareTemplateRequest fareTemplateRequest) {

        if (fareTemplateRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare template request is required"
            );
        }

        validateTemplate(fareTemplateRequest);

        FareTemplate existing = findFareTemplate(id);

        fareTemplateRepository.findByFlightIdAndCabinAndFareClass(
                        fareTemplateRequest.getFlightId(),
                        fareTemplateRequest.getCabin(),
                        fareTemplateRequest.getFareClass()
                )
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Another template already exists for this flight, cabin and fare class"
                    );
                });

        fareTemplateMapper.updateEntity(
                fareTemplateRequest,
                existing,
                fareRuleService.findActiveFareRule(fareTemplateRequest.getFareRuleId())
        );

        return fareTemplateMapper.toResponse(fareTemplateRepository.save(existing));
    }

    // Deactivate a fare template (soft delete)
    @Override
    @Transactional
    public void deactivateFareTemplate(Long id) {

        FareTemplate existing = findFareTemplate(id);

        existing.setActive(false);

        fareTemplateRepository.save(existing);
    }

    // Copy active templates onto a newly synced flight instance; existing fares are never rewritten
    @Override
    public int applyTemplatesToInstance(Long flightInstanceId, Long flightId, Long airlineId,
                                        LocalDateTime departureAt) {

        List<FareTemplate> templates = fareTemplateRepository
                .findAllByFlightIdAndAirlineIdAndIsActiveTrue(flightId, airlineId);

        int created = 0;

        for (FareTemplate template : templates) {
            if (fareService.applyTemplateToInstance(flightInstanceId, template) != null) {
                created++;
            }
        }

        return created;
    }

    private FareTemplate findFareTemplate(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare template ID must be a positive number"
            );
        }

        return fareTemplateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fare template not found with ID: " + id
                ));
    }

    private void validateTemplate(FareTemplateRequest request) {

        if (request.getBasePrice().signum() < 0
                || request.getTaxRate().signum() < 0
                || request.getTaxRate().compareTo(BigDecimal.ONE) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "basePrice must be non-negative and taxRate must be between 0 and 1"
            );
        }

        // Deactivating the referenced rule would silently break template application.
        fareRuleService.findActiveFareRule(request.getFareRuleId());
    }
}
