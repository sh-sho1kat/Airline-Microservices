package com.sho1kat.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CityRequest {

    @NotBlank(message = "City Name is Required")
    @Size(max = 100, message = "City Name cannot exceed 100 characters")
    private String cityName;

    @NotBlank(message = "City Code is Required")
    private String cityCode;

    @NotBlank(message = "Country Code is Required")
    @Size(min = 2, max = 3, message = "Country Code must contain 2-3 characters")
    private String countryCode;

    @NotBlank(message = "Country Name is Required")
    @Size(max = 100, message = "Country Name cannot exceed 100 characters")
    private String countryName;

    @Size(max = 20, message = "Region Code cannot exceed 20 characters")
    private String regionCode;

    @Size(max = 100, message = "Time Zone ID cannot exceed 100 characters")
    private String timeZoneId;
}