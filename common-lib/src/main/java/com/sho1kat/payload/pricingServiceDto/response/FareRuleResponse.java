package com.sho1kat.payload.pricingServiceDto.response;

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
public class FareRuleResponse {

    private Long id;

    private String name;

    private Boolean refundable;
    private Integer refundPercent;
    private BigDecimal changeFee;

    private Integer minAdvanceDays;
    private Integer maxStayDays;
    private Integer cancelCutoffHours;
    private BigDecimal noShowPenalty;

    private Boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;
}
