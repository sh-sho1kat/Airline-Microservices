package com.sho1kat.repository;

import com.sho1kat.entity.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    Optional<CouponRedemption> findByBookingReference(String bookingReference);

    long countByCoupon_IdAndUserId(Long couponId, Long userId);
}
