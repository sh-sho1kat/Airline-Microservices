package com.sho1kat.payload.pricingServiceDto.response;

import com.sho1kat.enums.CabinClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareResponse {

    private Long id;

    private Long flightInstanceId;

    private CabinClass cabin;
    private String fareClass;

    private BigDecimal basePrice;
    private String currency;
    private BigDecimal taxRate;

    // Base price including tax.
    private BigDecimal totalPrice;

    private Long fareRuleId;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    private Boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;
}
