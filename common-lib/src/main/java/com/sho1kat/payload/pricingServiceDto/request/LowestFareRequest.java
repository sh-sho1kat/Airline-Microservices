package com.sho1kat.payload.pricingServiceDto.request;

import com.sho1kat.enums.CabinClass;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LowestFareRequest {

    @NotEmpty(message = "Flight instance IDs are required")
    @Size(max = 200, message = "A lowest-fare request cannot contain more than 200 flight instances")
    private List<@NotNull(message = "Flight instance ID cannot be null")
                 @Positive(message = "Flight instance ID must be positive") Long> flightInstanceIds;

    @NotNull(message = "Cabin class is required")
    private CabinClass cabin;
}
