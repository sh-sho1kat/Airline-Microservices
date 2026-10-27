package com.sho1kat.mapper;

import com.sho1kat.entity.BaggagePolicy;
import com.sho1kat.payload.pricingServiceDto.request.BaggagePolicyRequest;
import com.sho1kat.payload.pricingServiceDto.response.BaggagePolicyResponse;
import org.springframework.stereotype.Component;

@Component
public class BaggagePolicyMapper {

    public BaggagePolicy toEntity(BaggagePolicyRequest request) {

        BaggagePolicy policy = new BaggagePolicy();

        policy.setAirlineId(request.getAirlineId());
        policy.setCabin(request.getCabin());
        policy.setCabinBaggageKg(request.getCabinBaggageKg());
        policy.setCheckedPieces(request.getCheckedPieces());
        policy.setCheckedKg(request.getCheckedKg());
        policy.setExtraKgFee(request.getExtraKgFee());
        policy.setActive(request.getIsActive() == null || request.getIsActive());

        return policy;
    }

    public void updateEntity(BaggagePolicyRequest request, BaggagePolicy existing) {

        if (request == null || existing == null) {
            return;
        }

        existing.setAirlineId(request.getAirlineId());
        existing.setCabin(request.getCabin());
        existing.setCabinBaggageKg(request.getCabinBaggageKg());
        existing.setCheckedPieces(request.getCheckedPieces());
        existing.setCheckedKg(request.getCheckedKg());
        existing.setExtraKgFee(request.getExtraKgFee());

        if (request.getIsActive() != null) {
            existing.setActive(request.getIsActive());
        }
    }

    public BaggagePolicyResponse toResponse(BaggagePolicy policy) {

        if (policy == null) {
            return null;
        }

        return BaggagePolicyResponse.builder()
                .id(policy.getId())
                .airlineId(policy.getAirlineId())
                .cabin(policy.getCabin())
                .cabinBaggageKg(policy.getCabinBaggageKg())
                .checkedPieces(policy.getCheckedPieces())
                .checkedKg(policy.getCheckedKg())
                .extraKgFee(policy.getExtraKgFee())
                .isActive(policy.isActive())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
