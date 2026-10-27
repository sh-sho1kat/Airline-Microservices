package com.sho1kat.payload.pricingServiceDto.request;

import com.sho1kat.enums.CabinClass;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FareRequest {

    @NotNull(message = "Flight instance ID is required")
    @Positive(message = "Flight instance ID must be positive")
    private Long flightInstanceId;

    @NotNull(message = "Fare rule ID is required")
    @Positive(message = "Fare rule ID must be positive")
    private Long fareRuleId;

    @NotNull(message = "Cabin class is required")
    private CabinClass cabin;

    @NotBlank(message = "Fare class is required")
    @Pattern(regexp = "[A-Z0-9]", message = "Fare class must be one uppercase letter or digit")
    private String fareClass;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.00", message = "Base price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Base price must have up to 10 digits and 2 decimals")
    private BigDecimal basePrice;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter uppercase code")
    private String currency;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0000", message = "Tax rate cannot be negative")
    @DecimalMax(value = "1.0000", message = "Tax rate cannot exceed 1.0000")
    @Digits(integer = 1, fraction = 4, message = "Tax rate must have up to 1 digit and 4 decimals")
    private BigDecimal taxRate;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    private Boolean isActive = true;
}
