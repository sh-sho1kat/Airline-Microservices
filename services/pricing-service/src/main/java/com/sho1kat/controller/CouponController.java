
package com.sho1kat.controller;

import com.sho1kat.payload.pricingServiceDto.request.CouponRedemptionRequest;
import com.sho1kat.payload.pricingServiceDto.request.CouponRequest;
import com.sho1kat.payload.pricingServiceDto.response.CouponRedemptionResponse;
import com.sho1kat.payload.pricingServiceDto.response.CouponResponse;
import com.sho1kat.service.CouponService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // Create a coupon
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<CouponResponse> createCoupon(
            @Valid @RequestBody CouponRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(couponService.createCoupon(request));
    }

    // Get all coupons; default includes inactive coupons
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<List<CouponResponse>> getAllCoupons(
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        return ResponseEntity.ok(
                couponService.getAllCoupons(activeOnly)
        );
    }

    // Get a coupon by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<CouponResponse> getCouponById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                couponService.getCouponById(id)
        );
    }

    // Update a coupon
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {

        return ResponseEntity.ok(
                couponService.updateCoupon(id, request)
        );
    }

    // Deactivate a coupon (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<Void> deactivateCoupon(
            @PathVariable Long id) {

        couponService.deactivateCoupon(id);

        return ResponseEntity.noContent().build();
    }

    // Record a coupon redemption after the booking service confirms a booking
    @PostMapping("/{code}/redemptions")
    @PreAuthorize("hasAnyAuthority('ROLE_BOOKING_SERVICE', 'ROLE_ADMIN', 'ROLE_AIRLINE_ADMIN')")
    public ResponseEntity<CouponRedemptionResponse> redeemCoupon(
            @PathVariable String code,
            @Valid @RequestBody CouponRedemptionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(couponService.redeemCoupon(code, request));
    }
}
