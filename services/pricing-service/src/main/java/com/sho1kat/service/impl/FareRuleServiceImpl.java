package com.sho1kat.service.impl;

import com.sho1kat.entity.FareRule;
import com.sho1kat.mapper.FareRuleMapper;
import com.sho1kat.payload.pricingServiceDto.request.FareRuleRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareRuleResponse;
import com.sho1kat.repository.FareRuleRepository;
import com.sho1kat.service.FareRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository fareRuleRepository;
    private final FareRuleMapper fareRuleMapper;

    // Create a fare rule
    @Override
    @Transactional
    public FareRuleResponse createFareRule(FareRuleRequest fareRuleRequest) {

        if (fareRuleRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare rule request is required"
            );
        }

        validateRule(fareRuleRequest);

        FareRule rule = fareRuleMapper.toEntity(fareRuleRequest);

        return fareRuleMapper.toResponse(fareRuleRepository.save(rule));
    }

    // Get all fare rules
    @Override
    public List<FareRuleResponse> getAllFareRules() {
        return fareRuleRepository.findAll().stream()
                .map(fareRuleMapper::toResponse)
                .toList();
    }

    // Get a fare rule by ID
    @Override
    public FareRuleResponse getFareRuleById(Long id) {
        return fareRuleMapper.toResponse(findFareRule(id));
    }

    // Update a fare rule
    @Override
    @Transactional
    public FareRuleResponse updateFareRule(Long id, FareRuleRequest fareRuleRequest) {

        if (fareRuleRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare rule request is required"
            );
        }

        validateRule(fareRuleRequest);

        FareRule existing = findFareRule(id);

        fareRuleMapper.updateEntity(fareRuleRequest, existing);

        return fareRuleMapper.toResponse(fareRuleRepository.save(existing));
    }

    // Delete deactivates; it does not physically delete rule history
    @Override
    @Transactional
    public void deactivateFareRule(Long id) {

        FareRule rule = findFareRule(id);

        rule.setActive(false);

        fareRuleRepository.save(rule);
    }

    // Find an active rule or fail; used by fares and templates
    @Override
    public FareRule findActiveFareRule(Long id) {

        FareRule rule = findFareRule(id);

        if (!rule.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare rule is inactive: " + id
            );
        }

        return rule;
    }

    private FareRule findFareRule(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare rule ID must be a positive number"
            );
        }

        return fareRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fare rule not found with ID: " + id
                ));
    }

    private void validateRule(FareRuleRequest request) {

        if (!Boolean.TRUE.equals(request.getRefundable())
                && request.getRefundPercent() != null
                && request.getRefundPercent() != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Non-refundable fare rules must have refundPercent = 0"
            );
        }
    }
}
