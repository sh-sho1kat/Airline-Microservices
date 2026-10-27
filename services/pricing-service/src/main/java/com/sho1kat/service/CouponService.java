package com.sho1kat.service;

import com.sho1kat.payload.pricingServiceDto.request.CouponRedemptionRequest;
import com.sho1kat.payload.pricingServiceDto.request.CouponRequest;
import com.sho1kat.payload.pricingServiceDto.response.CouponRedemptionResponse;
import com.sho1kat.payload.pricingServiceDto.response.CouponResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    CouponResponse createCoupon(CouponRequest couponRequest);

    List<CouponResponse> getAllCoupons(boolean activeOnly);

    CouponResponse getCouponById(Long id);

    CouponResponse updateCoupon(Long id, CouponRequest couponRequest);

    void deactivateCoupon(Long id);

    /*
     * Validates a coupon and computes its discount for a public quote.
     * Quoting never increments usage.
     */
    BigDecimal calculateDiscountForQuote(String code, BigDecimal eligibleBaseAmount, String currency,
                                         Long airlineId, com.sho1kat.enums.CabinClass cabin);

    /*
     * Called after the booking service confirms a booking.
     * The booking reference makes this idempotent.
     */
    CouponRedemptionResponse redeemCoupon(String code, CouponRedemptionRequest redemptionRequest);
}
