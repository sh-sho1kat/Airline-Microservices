package com.sho1kat.service;

import com.sho1kat.payload.pricingServiceDto.request.BaggagePolicyRequest;
import com.sho1kat.payload.pricingServiceDto.response.BaggagePolicyResponse;

import java.util.List;

public interface BaggagePolicyService {

    BaggagePolicyResponse createBaggagePolicy(BaggagePolicyRequest baggagePolicyRequest);

    BaggagePolicyResponse findActiveForAirlineAndCabin(Long airlineId, com.sho1kat.enums.CabinClass cabin);

    List<BaggagePolicyResponse> getAllBaggagePolicies(Long airlineId);

    BaggagePolicyResponse updateBaggagePolicy(Long id, BaggagePolicyRequest baggagePolicyRequest);

    void deactivateBaggagePolicy(Long id);
}
