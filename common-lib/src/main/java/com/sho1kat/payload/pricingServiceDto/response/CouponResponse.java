package com.sho1kat.payload.pricingServiceDto.response;

import com.sho1kat.enums.CabinClass;
import com.sho1kat.enums.DiscountType;
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
public class CouponResponse {

    private Long id;

    private String code;
    private String description;

    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;

    private BigDecimal minBookingAmount;
    private String currency;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private Integer maxUses;
    private Integer maxUsesPerUser;
    private Integer usedCount;

    private Long airlineId;
    private CabinClass cabin;

    private Boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;
}
