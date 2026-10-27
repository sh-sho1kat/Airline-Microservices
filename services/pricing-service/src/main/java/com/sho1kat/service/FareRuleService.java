package com.sho1kat.service;

import com.sho1kat.entity.FareRule;
import com.sho1kat.payload.pricingServiceDto.request.FareRuleRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareRuleResponse;

import java.util.List;

public interface FareRuleService {

    FareRuleResponse createFareRule(FareRuleRequest fareRuleRequest);

    List<FareRuleResponse> getAllFareRules();

    FareRuleResponse getFareRuleById(Long id);

    FareRuleResponse updateFareRule(Long id, FareRuleRequest fareRuleRequest);

    void deactivateFareRule(Long id);

    FareRule findActiveFareRule(Long id);
}
