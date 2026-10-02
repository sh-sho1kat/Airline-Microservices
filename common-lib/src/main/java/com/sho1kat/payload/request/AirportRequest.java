package com.sho1kat.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.ZoneId;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirportRequest {

    @NotBlank(message = "IATA Code is mandatory")
    @Size(min = 3, max = 3)
    @Pattern(
            regexp = "^[A-Za-z]{3}$",
            message = "IATA code must contain exactly 3 letters"
    )
    private String iataCode;

    @NotBlank
    private String name;

    @NotBlank
    private String timeZoneId;

    @Valid
    private AddressRequest address;

    @Valid
    private GeoCodeRequest geoCode;

    @NotNull
    private Long cityId;
}