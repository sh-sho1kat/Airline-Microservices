package com.sho1kat.payload.pricingServiceDto.request;

import com.sho1kat.enums.CabinClass;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CouponRedemptionRequest {

    @NotBlank(message = "Booking reference is required")
    @Size(max = 100, message = "Booking reference cannot exceed 100 characters")
    private String bookingReference;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Booking base amount is required")
    @DecimalMin(value = "0.00", message = "Booking base amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Booking base amount must have up to 10 digits and 2 decimals")
    private BigDecimal bookingBaseAmount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter uppercase code")
    private String currency;

    @NotNull(message = "Airline ID is required")
    @Positive(message = "Airline ID must be positive")
    private Long airlineId;

    @NotNull(message = "Cabin class is required")
    private CabinClass cabin;
}
