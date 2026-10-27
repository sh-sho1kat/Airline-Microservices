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
public class BaggagePolicyResponse {

    private Long id;

    private Long airlineId;
    private CabinClass cabin;

    private BigDecimal cabinBaggageKg;

    private Integer checkedPieces;
    private BigDecimal checkedKg;

    private BigDecimal extraKgFee;

    private Boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;
}
