package com.sho1kat.payload.pricingServiceDto.response;

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
public class CouponRedemptionResponse {

    private Long id;

    private String couponCode;
    private String bookingReference;
    private Long userId;

    private BigDecimal discountAmount;

    private LocalDateTime redeemedAt;
    private Instant createdAt;
}
