package com.sho1kat.payload.pricingServiceDto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class FareRuleRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Refundable flag is required")
    private Boolean refundable;

    @NotNull(message = "Refund percent is required")
    @Min(value = 0, message = "Refund percent cannot be negative")
    @Max(value = 100, message = "Refund percent cannot exceed 100")
    private Integer refundPercent;

    @NotNull(message = "Change fee is required")
    @DecimalMin(value = "0.00", message = "Change fee cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Change fee must have up to 10 digits and 2 decimals")
    private BigDecimal changeFee;

    @NotNull(message = "Minimum advance days is required")
    @PositiveOrZero(message = "Minimum advance days cannot be negative")
    private Integer minAdvanceDays;

    @PositiveOrZero(message = "Maximum stay days cannot be negative")
    private Integer maxStayDays;

    @NotNull(message = "Cancel cutoff hours is required")
    @PositiveOrZero(message = "Cancel cutoff hours cannot be negative")
    private Integer cancelCutoffHours;

    @NotNull(message = "No-show penalty is required")
    @DecimalMin(value = "0.00", message = "No-show penalty cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "No-show penalty must have up to 10 digits and 2 decimals")
    private BigDecimal noShowPenalty;

    private Boolean isActive = true;
}
