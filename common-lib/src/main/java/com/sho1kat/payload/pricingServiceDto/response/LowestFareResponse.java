package com.sho1kat.payload.pricingServiceDto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowestFareResponse {

    private List<Price> prices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Price {

        private Long flightInstanceId;

        private Long fareId;
        private BigDecimal amount;
        private String currency;
    }
}
