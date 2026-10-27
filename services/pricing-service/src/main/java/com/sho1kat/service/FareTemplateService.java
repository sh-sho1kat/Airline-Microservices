package com.sho1kat.service;

import com.sho1kat.payload.pricingServiceDto.request.FareTemplateRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareTemplateResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface FareTemplateService {

    FareTemplateResponse createFareTemplate(FareTemplateRequest fareTemplateRequest);

    List<FareTemplateResponse> getAllFareTemplates();

    FareTemplateResponse getFareTemplateById(Long id);

    FareTemplateResponse updateFareTemplate(Long id, FareTemplateRequest fareTemplateRequest);

    void deactivateFareTemplate(Long id);

    int applyTemplatesToInstance(Long flightInstanceId, Long flightId, Long airlineId, LocalDateTime departureAt);
}
