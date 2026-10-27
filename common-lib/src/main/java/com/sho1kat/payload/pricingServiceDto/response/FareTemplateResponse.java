package com.sho1kat.payload.pricingServiceDto.response;

import com.sho1kat.enums.CabinClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareTemplateResponse {

    private Long id;

    private Long airlineId;
    private Long flightId;

    private CabinClass cabin;
    private String fareClass;

    private BigDecimal basePrice;
    private String currency;
    private BigDecimal taxRate;

    private Long fareRuleId;

    private Boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;
}
