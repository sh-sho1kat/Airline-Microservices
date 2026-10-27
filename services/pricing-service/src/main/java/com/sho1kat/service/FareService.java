package com.sho1kat.service;

import com.sho1kat.entity.Fare;
import com.sho1kat.entity.FareTemplate;
import com.sho1kat.payload.pricingServiceDto.request.FareRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareResponse;
import com.sho1kat.payload.pricingServiceDto.response.LowestFareResponse;
import com.sho1kat.payload.pricingServiceDto.response.UnpricedFlightResponse;

import java.time.LocalDate;
import java.util.List;

public interface FareService {

    FareResponse createFare(FareRequest fareRequest);

    List<FareResponse> createFaresInBulk(List<FareRequest> fareRequests);

    List<FareResponse> getFaresByFlightInstanceId(Long flightInstanceId);

    FareResponse getFareById(Long fareId);

    FareResponse updateFare(Long fareId, FareRequest fareRequest);

    void deactivateFare(Long fareId);

    LowestFareResponse findLowestFares(com.sho1kat.payload.pricingServiceDto.request.LowestFareRequest lowestFareRequest);

    List<UnpricedFlightResponse> findUnpricedInstances(Long flightId, LocalDate from, LocalDate to);

    Fare applyTemplateToInstance(Long flightInstanceId, FareTemplate template);

    boolean isFareCurrentlyValid(Fare fare);
}
