package com.sho1kat.service.impl;

import com.sho1kat.entity.Fare;
import com.sho1kat.entity.FlightInstanceReference;
import com.sho1kat.enums.CabinClass;
import com.sho1kat.enums.PaxType;
import com.sho1kat.payload.pricingServiceDto.response.QuoteResponse;
import com.sho1kat.repository.FareRepository;
import com.sho1kat.repository.FlightInstanceReferenceRepository;
import com.sho1kat.service.CouponService;
import com.sho1kat.service.FareQuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FareQuoteServiceImpl implements FareQuoteService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    // Quote validity at most 10 minutes; shortened by the fare's own validity end.
    private static final int QUOTE_VALIDITY_MINUTES = 10;

    private final FareRepository fareRepository;
    private final CouponService couponService;
    private final FlightInstanceReferenceRepository flightInstanceReferenceRepository;

    // Passenger pricing policy; override together with the airline before quoting children.
    @Value("${pricing.child-fare-percentage:100}")
    private BigDecimal childFarePercentage;

    @Value("${pricing.infant-fare-percentage:10}")
    private BigDecimal infantFarePercentage;

    // Quote passenger pricing; fare class and coupon are optional
    @Override
    public QuoteResponse quoteFare(Long flightInstanceId, CabinClass cabin, int adults, int children,
                                   int infants, String fareClass, String couponCode) {

        if (adults < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one adult is required"
            );
        }

        if (children < 0 || infants < 0 || infants > adults) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Counts must be non-negative and infants cannot exceed adults"
            );
        }

        List<Fare> candidates = findCandidates(flightInstanceId, cabin, fareClass);

        LocalDateTime now = LocalDateTime.now();

        candidates = candidates.stream()
                .filter(fare -> isFareCurrentlyValid(fare, now))
                .toList();

        if (candidates.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No active, valid fare found for this flight instance and cabin"
            );
        }

        FlightInstanceReference reference = flightInstanceReferenceRepository
                .findById(flightInstanceId)
                .orElse(null);

        if (reference != null && !Boolean.TRUE.equals(reference.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Flight instance is inactive"
            );
        }

        List<QuoteResponse> quotes = new ArrayList<>();

        for (Fare fare : candidates) {
            quotes.add(buildQuote(fare, adults, children, infants, couponCode,
                    reference == null ? null : reference.getAirlineId(), now));
        }

        // Pick the cheapest candidate; ties break by fare class and fare ID for determinism.
        return quotes.stream()
                .min(Comparator.comparing(QuoteResponse::getTotal)
                        .thenComparing(QuoteResponse::getFareClass)
                        .thenComparing(QuoteResponse::getFareId))
                .orElseThrow();
    }

    private List<Fare> findCandidates(Long flightInstanceId, CabinClass cabin, String fareClass) {

        if (fareClass == null || fareClass.isBlank()) {
            return fareRepository.findAllByFlightInstanceIdAndCabinAndIsActiveTrue(flightInstanceId, cabin);
        }

        return fareRepository
                .findByFlightInstanceIdAndCabinAndFareClass(
                        flightInstanceId,
                        cabin,
                        fareClass.trim().toUpperCase(Locale.ROOT)
                )
                .filter(Fare::getIsActive)
                .stream()
                .toList();
    }

    private QuoteResponse buildQuote(Fare fare, int adults, int children, int infants, String couponCode,
                                     Long airlineId, LocalDateTime now) {

        List<QuoteResponse.Line> lines = new ArrayList<>();

        addLine(lines, PaxType.ADULT, adults, fare.getBasePrice(), HUNDRED);

        if (children > 0) {
            addLine(lines, PaxType.CHILD, children, fare.getBasePrice(), childFarePercentage);
        }

        if (infants > 0) {
            addLine(lines, PaxType.INFANT, infants, fare.getBasePrice(), infantFarePercentage);
        }

        BigDecimal baseFare = lines.stream()
                .map(QuoteResponse.Line::getSubtotal)
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Coupon applies to the passenger base-fare subtotal before taxes; usage is only
        // reserved by the redemption endpoint after booking confirmation.
        BigDecimal discount = couponCode == null || couponCode.isBlank()
                ? ZERO
                : couponService.calculateDiscountForQuote(
                        couponCode, baseFare, fare.getCurrency(), airlineId, fare.getCabin());

        BigDecimal taxableBase = baseFare.subtract(discount).max(ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxes = taxableBase.multiply(fare.getTaxRate())
                .setScale(2, RoundingMode.HALF_UP);

        /*
         * Fees stay zero because the fare schema has no separate service-fee field.
         */
        BigDecimal fees = ZERO;

        BigDecimal total = taxableBase.add(taxes).add(fees)
                .setScale(2, RoundingMode.HALF_UP);

        LocalDateTime validUntil = now.plusMinutes(QUOTE_VALIDITY_MINUTES);

        if (fare.getValidTo() != null && fare.getValidTo().isBefore(validUntil)) {
            validUntil = fare.getValidTo();
        }

        return QuoteResponse.builder()
                .flightInstanceId(fare.getFlightInstanceId())
                .fareId(fare.getId())
                .cabin(fare.getCabin())
                .fareClass(fare.getFareClass())
                .lines(lines)
                .baseFare(baseFare)
                .couponCode(couponCode == null || couponCode.isBlank()
                        ? null
                        : couponCode.trim().toUpperCase(Locale.ROOT))
                .discount(discount)
                .taxes(taxes)
                .fees(fees)
                .total(total)
                .currency(fare.getCurrency())
                .validUntil(validUntil)
                .fareRuleId(fare.getFareRule() != null ? fare.getFareRule().getId() : null)
                .build();
    }

    private void addLine(List<QuoteResponse.Line> lines, PaxType type, int count,
                         BigDecimal adultPrice, BigDecimal percentage) {

        if (count == 0) {
            return;
        }

        BigDecimal unitPrice = adultPrice.multiply(percentage)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(count))
                .setScale(2, RoundingMode.HALF_UP);

        lines.add(QuoteResponse.Line.builder()
                .paxType(type)
                .count(count)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build());
    }

    private boolean isFareCurrentlyValid(Fare fare, LocalDateTime now) {
        return (fare.getValidFrom() == null || !fare.getValidFrom().isAfter(now))
                && (fare.getValidTo() == null || !fare.getValidTo().isBefore(now));
    }
}
