package com.sho1kat.service.impl;

import com.sho1kat.entity.CouponRedemption;
import com.sho1kat.entity.DiscountCoupon;
import com.sho1kat.enums.CabinClass;
import com.sho1kat.enums.DiscountType;
import com.sho1kat.mapper.CouponMapper;
import com.sho1kat.payload.pricingServiceDto.request.CouponRedemptionRequest;
import com.sho1kat.payload.pricingServiceDto.request.CouponRequest;
import com.sho1kat.payload.pricingServiceDto.response.CouponRedemptionResponse;
import com.sho1kat.payload.pricingServiceDto.response.CouponResponse;
import com.sho1kat.repository.CouponRedemptionRepository;
import com.sho1kat.repository.DiscountCouponRepository;
import com.sho1kat.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final DiscountCouponRepository discountCouponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final CouponMapper couponMapper;

    // Create a coupon
    @Override
    @Transactional
    public CouponResponse createCoupon(CouponRequest couponRequest) {

        if (couponRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon request is required"
            );
        }

        validateCoupon(couponRequest);

        String code = couponMapper.normalizeCode(couponRequest.getCode());

        if (discountCouponRepository.findByCode(code).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Coupon code already exists: " + code
            );
        }

        DiscountCoupon coupon = couponMapper.toEntity(couponRequest);

        return couponMapper.toResponse(discountCouponRepository.save(coupon));
    }

    // Get all coupons including inactive ones by default
    @Override
    public List<CouponResponse> getAllCoupons(boolean activeOnly) {

        return (activeOnly
                ? discountCouponRepository.findAllByIsActiveTrue()
                : discountCouponRepository.findAll())
                .stream()
                .map(couponMapper::toResponse)
                .toList();
    }

    // Get a coupon by ID
    @Override
    public CouponResponse getCouponById(Long id) {
        return couponMapper.toResponse(findCoupon(id));
    }

    // Update a coupon
    @Override
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest couponRequest) {

        if (couponRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon request is required"
            );
        }

        validateCoupon(couponRequest);

        DiscountCoupon existing = findCoupon(id);

        String code = couponMapper.normalizeCode(couponRequest.getCode());

        discountCouponRepository.findByCode(code)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Coupon code already exists: " + code
                    );
                });

        couponMapper.updateEntity(couponRequest, existing);

        return couponMapper.toResponse(discountCouponRepository.save(existing));
    }

    // Deactivate a coupon (soft delete)
    @Override
    @Transactional
    public void deactivateCoupon(Long id) {

        DiscountCoupon existing = findCoupon(id);

        existing.setActive(false);

        discountCouponRepository.save(existing);
    }

    // Validates a coupon and computes its discount; quotes never reserve usage
    @Override
    @Transactional
    public BigDecimal calculateDiscountForQuote(String code, BigDecimal eligibleBaseAmount, String currency,
                                                Long airlineId, CabinClass cabin) {

        if (code == null || code.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        DiscountCoupon coupon = discountCouponRepository.findByCode(couponMapper.normalizeCode(code))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid coupon code"
                ));

        validateUsable(coupon, eligibleBaseAmount, currency, airlineId, cabin, true);

        return calculateDiscount(coupon, eligibleBaseAmount);
    }

    // Called by the booking service after confirmation; idempotent per booking reference
    @Override
    @Transactional
    public CouponRedemptionResponse redeemCoupon(String code, CouponRedemptionRequest redemptionRequest) {

        if (redemptionRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon redemption request is required"
            );
        }

        CouponRedemption existing =
                couponRedemptionRepository.findByBookingReference(redemptionRequest.getBookingReference()).orElse(null);

        if (existing != null) {

            boolean sameCoupon = existing.getCoupon() != null
                    && existing.getCoupon().getCode().equalsIgnoreCase(couponMapper.normalizeCode(code));

            if (sameCoupon && existing.getUserId().equals(redemptionRequest.getUserId())) {
                return couponMapper.toRedemptionResponse(existing);
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This booking reference already has a coupon redemption"
            );
        }

        DiscountCoupon coupon = discountCouponRepository
                .findByCodeForUpdate(couponMapper.normalizeCode(code))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Coupon not found: " + code
                ));

        validateUsable(coupon, redemptionRequest.getBookingBaseAmount(), redemptionRequest.getCurrency(),
                redemptionRequest.getAirlineId(), redemptionRequest.getCabin(), true);

        if (coupon.getMaxUsesPerUser() != null && couponRedemptionRepository
                .countByCoupon_IdAndUserId(coupon.getId(), redemptionRequest.getUserId()) >= coupon.getMaxUsesPerUser()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User has reached the usage limit for this coupon"
            );
        }

        BigDecimal discount = calculateDiscount(coupon, redemptionRequest.getBookingBaseAmount());

        CouponRedemption redemption = new CouponRedemption();

        redemption.setCoupon(coupon);
        redemption.setBookingReference(redemptionRequest.getBookingReference().trim());
        redemption.setUserId(redemptionRequest.getUserId());
        redemption.setDiscountAmount(discount);
        redemption.setRedeemedAt(LocalDateTime.now());

        CouponRedemption saved = couponRedemptionRepository.save(redemption);

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        discountCouponRepository.save(coupon);

        return couponMapper.toRedemptionResponse(saved);
    }

    private DiscountCoupon findCoupon(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon ID must be a positive number"
            );
        }

        return discountCouponRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Coupon not found with ID: " + id
                ));
    }

    private void validateCoupon(CouponRequest request) {

        if (!request.getValidUntil().isAfter(request.getValidFrom())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "validUntil must be after validFrom"
            );
        }

        if (request.getDiscountValue().signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "discountValue must be greater than zero"
            );
        }

        if (request.getDiscountType() == DiscountType.PERCENTAGE
                && request.getDiscountValue().compareTo(HUNDRED) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Percentage discount cannot exceed 100"
            );
        }

        if (request.getMaxDiscountAmount() != null && request.getMaxDiscountAmount().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "maxDiscountAmount cannot be negative"
            );
        }
    }

    private void validateUsable(DiscountCoupon coupon, BigDecimal amount, String currency,
                                Long airlineId, CabinClass cabin, boolean checkGlobalUsage) {

        LocalDateTime now = LocalDateTime.now();

        if (!coupon.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon is inactive"
            );
        }

        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon is outside its validity period"
            );
        }

        if (checkGlobalUsage && coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Coupon usage limit has been reached"
            );
        }

        if (!coupon.getCurrency().equalsIgnoreCase(currency)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon currency does not match fare currency"
            );
        }

        if (amount.compareTo(coupon.getMinBookingAmount()) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking amount does not meet the coupon minimum"
            );
        }

        if (coupon.getAirlineId() != null && (airlineId == null || !coupon.getAirlineId().equals(airlineId))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon is not valid for this airline"
            );
        }

        if (coupon.getCabin() != null && coupon.getCabin() != cabin) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon is not valid for this cabin"
            );
        }
    }

    private BigDecimal calculateDiscount(DiscountCoupon coupon, BigDecimal amount) {

        BigDecimal discount;

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = amount.multiply(coupon.getDiscountValue())
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);

            if (coupon.getMaxDiscountAmount() != null) {
                discount = discount.min(coupon.getMaxDiscountAmount());
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        return discount.min(amount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
