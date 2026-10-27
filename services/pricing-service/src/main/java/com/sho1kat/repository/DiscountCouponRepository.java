package com.sho1kat.repository;

import com.sho1kat.entity.DiscountCoupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiscountCouponRepository extends JpaRepository<DiscountCoupon, Long> {

    Optional<DiscountCoupon> findByCode(String code);

    List<DiscountCoupon> findAllByIsActiveTrue();

    // Used by redemption so concurrent bookings cannot exceed the usage limit.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from DiscountCoupon c where c.code = :code")
    Optional<DiscountCoupon> findByCodeForUpdate(@Param("code") String code);
}
