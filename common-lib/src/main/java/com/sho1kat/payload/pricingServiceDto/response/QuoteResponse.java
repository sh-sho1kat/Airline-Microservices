package com.sho1kat.payload.pricingServiceDto.response;

import com.sho1kat.enums.CabinClass;
import com.sho1kat.enums.PaxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {

    private Long flightInstanceId;

    private Long fareId;
    private CabinClass cabin;
    private String fareClass;

    private List<Line> lines;

    // Passenger base-fare subtotal before discount and taxes.
    private BigDecimal baseFare;

    private String couponCode;
    private BigDecimal discount;
    private BigDecimal taxes;
    private BigDecimal fees;
    private BigDecimal total;

    private String currency;

    private LocalDateTime validUntil;
    private Long fareRuleId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Line {

        private PaxType paxType;

        private Integer count;

        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
