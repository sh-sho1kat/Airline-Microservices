package com.sho1kat.payload.pricingServiceDto.request;

import com.sho1kat.enums.CabinClass;
import com.sho1kat.enums.DiscountType;
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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CouponRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 40, message = "Code cannot exceed 40 characters")
    @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Code may contain letters, digits, underscore and hyphen")
    private String code;

    @Size(max = 250, message = "Description cannot exceed 250 characters")
    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Discount value must have up to 10 digits and 2 decimals")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Maximum discount amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Maximum discount amount must have up to 10 digits and 2 decimals")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Minimum booking amount is required")
    @DecimalMin(value = "0.00", message = "Minimum booking amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Minimum booking amount must have up to 10 digits and 2 decimals")
    private BigDecimal minBookingAmount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter uppercase code")
    private String currency;

    @NotNull(message = "Valid from is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until is required")
    private LocalDateTime validUntil;

    @Positive(message = "Maximum uses must be positive")
    private Integer maxUses;

    @Positive(message = "Maximum uses per user must be positive")
    private Integer maxUsesPerUser;

    @Positive(message = "Airline ID must be positive")
    private Long airlineId;

    private CabinClass cabin;

    private Boolean isActive = true;
}
