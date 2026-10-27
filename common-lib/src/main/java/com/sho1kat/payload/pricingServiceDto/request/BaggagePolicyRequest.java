package com.sho1kat.payload.pricingServiceDto.request;

import com.sho1kat.enums.CabinClass;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class BaggagePolicyRequest {

    @NotNull(message = "Airline ID is required")
    @Positive(message = "Airline ID must be positive")
    private Long airlineId;

    @NotNull(message = "Cabin class is required")
    private CabinClass cabin;

    @NotNull(message = "Cabin baggage allowance is required")
    @DecimalMin(value = "0.00", message = "Cabin baggage allowance cannot be negative")
    @Digits(integer = 4, fraction = 2, message = "Cabin baggage allowance must have up to 4 digits and 2 decimals")
    private BigDecimal cabinBaggageKg;

    @NotNull(message = "Checked pieces is required")
    @PositiveOrZero(message = "Checked pieces cannot be negative")
    private Integer checkedPieces;

    @NotNull(message = "Checked baggage allowance is required")
    @DecimalMin(value = "0.00", message = "Checked baggage allowance cannot be negative")
    @Digits(integer = 4, fraction = 2, message = "Checked baggage allowance must have up to 4 digits and 2 decimals")
    private BigDecimal checkedKg;

    @NotNull(message = "Extra kg fee is required")
    @DecimalMin(value = "0.00", message = "Extra kg fee cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Extra kg fee must have up to 10 digits and 2 decimals")
    private BigDecimal extraKgFee;

    private Boolean isActive = true;
}
