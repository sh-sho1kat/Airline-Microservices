package com.sho1kat.mapper;

import com.sho1kat.entity.CouponRedemption;
import com.sho1kat.entity.DiscountCoupon;
import com.sho1kat.payload.pricingServiceDto.request.CouponRequest;
import com.sho1kat.payload.pricingServiceDto.response.CouponRedemptionResponse;
import com.sho1kat.payload.pricingServiceDto.response.CouponResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CouponMapper {

    public DiscountCoupon toEntity(CouponRequest request) {

        DiscountCoupon coupon = new DiscountCoupon();

        apply(coupon, request);
        coupon.setActive(request.getIsActive() == null || request.getIsActive());

        return coupon;
    }

    public void updateEntity(CouponRequest request, DiscountCoupon existing) {

        if (request == null || existing == null) {
            return;
        }

        apply(existing, request);

        if (request.getIsActive() != null) {
            existing.setActive(request.getIsActive());
        }
    }

    public CouponResponse toResponse(DiscountCoupon coupon) {

        if (coupon == null) {
            return null;
        }

        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minBookingAmount(coupon.getMinBookingAmount())
                .currency(coupon.getCurrency())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .maxUses(coupon.getMaxUses())
                .maxUsesPerUser(coupon.getMaxUsesPerUser())
                .usedCount(coupon.getUsedCount())
                .airlineId(coupon.getAirlineId())
                .cabin(coupon.getCabin())
                .isActive(coupon.isActive())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    public CouponRedemptionResponse toRedemptionResponse(CouponRedemption redemption) {

        if (redemption == null) {
            return null;
        }

        return CouponRedemptionResponse.builder()
                .id(redemption.getId())
                .couponCode(redemption.getCoupon() != null ? redemption.getCoupon().getCode() : null)
                .bookingReference(redemption.getBookingReference())
                .userId(redemption.getUserId())
                .discountAmount(redemption.getDiscountAmount())
                .redeemedAt(redemption.getRedeemedAt())
                .createdAt(redemption.getCreatedAt())
                .build();
    }

    private void apply(DiscountCoupon coupon, CouponRequest request) {

        coupon.setCode(normalizeCode(request.getCode()));
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinBookingAmount(request.getMinBookingAmount());
        coupon.setCurrency(normalizeCurrency(request.getCurrency()));
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        coupon.setMaxUses(request.getMaxUses());
        coupon.setMaxUsesPerUser(request.getMaxUsesPerUser());
        coupon.setAirlineId(request.getAirlineId());
        coupon.setCabin(request.getCabin());
    }

    public String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCurrency(String currency) {
        return currency == null ? null : currency.trim().toUpperCase(Locale.ROOT);
    }
}
