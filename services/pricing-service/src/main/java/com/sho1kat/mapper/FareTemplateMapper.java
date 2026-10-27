package com.sho1kat.mapper;

import com.sho1kat.entity.FareRule;
import com.sho1kat.entity.FareTemplate;
import com.sho1kat.payload.pricingServiceDto.request.FareTemplateRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareTemplateResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class FareTemplateMapper {

    public FareTemplate toEntity(FareTemplateRequest request, FareRule fareRule) {

        FareTemplate template = new FareTemplate();

        template.setAirlineId(request.getAirlineId());
        template.setFlightId(request.getFlightId());
        template.setCabin(request.getCabin());
        template.setFareClass(normalizeFareClass(request.getFareClass()));
        template.setBasePrice(request.getBasePrice());
        template.setCurrency(normalizeCurrency(request.getCurrency()));
        template.setTaxRate(request.getTaxRate());
        template.setFareRule(fareRule);
        template.setActive(request.getIsActive() == null || request.getIsActive());

        return template;
    }

    public void updateEntity(FareTemplateRequest request, FareTemplate existing, FareRule fareRule) {

        if (request == null || existing == null) {
            return;
        }

        existing.setAirlineId(request.getAirlineId());
        existing.setFlightId(request.getFlightId());
        existing.setCabin(request.getCabin());
        existing.setFareClass(normalizeFareClass(request.getFareClass()));
        existing.setBasePrice(request.getBasePrice());
        existing.setCurrency(normalizeCurrency(request.getCurrency()));
        existing.setTaxRate(request.getTaxRate());
        existing.setFareRule(fareRule);

        if (request.getIsActive() != null) {
            existing.setActive(request.getIsActive());
        }
    }

    public FareTemplateResponse toResponse(FareTemplate template) {

        if (template == null) {
            return null;
        }

        return FareTemplateResponse.builder()
                .id(template.getId())
                .airlineId(template.getAirlineId())
                .flightId(template.getFlightId())
                .cabin(template.getCabin())
                .fareClass(template.getFareClass())
                .basePrice(template.getBasePrice())
                .currency(template.getCurrency())
                .taxRate(template.getTaxRate())
                .fareRuleId(template.getFareRule() != null ? template.getFareRule().getId() : null)
                .isActive(template.isActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private String normalizeFareClass(String fareClass) {
        return fareClass == null ? null : fareClass.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCurrency(String currency) {
        return currency == null ? null : currency.trim().toUpperCase(Locale.ROOT);
    }
}
