package com.sho1kat.mapper;

import com.sho1kat.entity.Fare;
import com.sho1kat.entity.FareRule;
import com.sho1kat.payload.pricingServiceDto.request.FareRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Component
public class FareMapper {

    public Fare toEntity(FareRequest request, FareRule fareRule) {

        Fare fare = new Fare();

        fare.setFlightInstanceId(request.getFlightInstanceId());
        fare.setCabin(request.getCabin());
        fare.setFareClass(normalizeFareClass(request.getFareClass()));
        fare.setBasePrice(request.getBasePrice());
        fare.setCurrency(normalizeCurrency(request.getCurrency()));
        fare.setTaxRate(request.getTaxRate());
        fare.setFareRule(fareRule);
        fare.setValidFrom(request.getValidFrom());
        fare.setValidTo(request.getValidTo());
        fare.setIsActive(request.getIsActive() == null || request.getIsActive());

        return fare;
    }

    public void updateEntity(FareRequest request, Fare existing, FareRule fareRule) {

        if (request == null || existing == null) {
            return;
        }

        existing.setCabin(request.getCabin());
        existing.setFareClass(normalizeFareClass(request.getFareClass()));
        existing.setBasePrice(request.getBasePrice());
        existing.setCurrency(normalizeCurrency(request.getCurrency()));
        existing.setTaxRate(request.getTaxRate());
        existing.setFareRule(fareRule);
        existing.setValidFrom(request.getValidFrom());
        existing.setValidTo(request.getValidTo());

        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        // flightInstanceId is immutable after creation.
    }

    // Copies a template onto a newly synced flight instance.
    public Fare toEntity(Long flightInstanceId, com.sho1kat.entity.FareTemplate template) {

        Fare fare = new Fare();

        fare.setFlightInstanceId(flightInstanceId);
        fare.setCabin(template.getCabin());
        fare.setFareClass(template.getFareClass());
        fare.setBasePrice(template.getBasePrice());
        fare.setCurrency(template.getCurrency());
        fare.setTaxRate(template.getTaxRate());
        fare.setFareRule(template.getFareRule());
        fare.setValidFrom(java.time.LocalDateTime.now());
        fare.setIsActive(true);

        return fare;
    }

    public FareResponse toResponse(Fare fare) {

        if (fare == null) {
            return null;
        }

        return FareResponse.builder()
                .id(fare.getId())
                .flightInstanceId(fare.getFlightInstanceId())
                .cabin(fare.getCabin())
                .fareClass(fare.getFareClass())
                .basePrice(fare.getBasePrice())
                .currency(fare.getCurrency())
                .taxRate(fare.getTaxRate())
                .totalPrice(calculateTotalPrice(fare))
                .fareRuleId(fare.getFareRule() != null ? fare.getFareRule().getId() : null)
                .validFrom(fare.getValidFrom())
                .validTo(fare.getValidTo())
                .isActive(fare.getIsActive())
                .createdAt(fare.getCreatedAt())
                .updatedAt(fare.getUpdatedAt())
                .build();
    }

    public BigDecimal calculateTotalPrice(Fare fare) {

        BigDecimal basePrice = fare.getBasePrice() == null ? BigDecimal.ZERO : fare.getBasePrice();
        BigDecimal taxRate = fare.getTaxRate() == null ? BigDecimal.ZERO : fare.getTaxRate();

        return basePrice
                .add(basePrice.multiply(taxRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeFareClass(String fareClass) {
        return fareClass == null ? null : fareClass.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCurrency(String currency) {
        return currency == null ? null : currency.trim().toUpperCase(Locale.ROOT);
    }
}
