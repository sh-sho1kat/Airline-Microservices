package com.sho1kat.payload.airlineServiceDto.request;

import com.sho1kat.enums.AirlineStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirlineRequest {
    @NotBlank(message = "IATA code is mandatory")
    @Size(min = 2, max = 2, message = "IATA code must be exactly 2 characters")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "IATA code must contain exactly 2 letters")
    private String iataCode;

    @NotBlank(message = "ICAO code is mandatory")
    @Size(min = 3, max = 3, message = "ICAO code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "ICAO code must contain exactly 3 letters")
    private String icaoCode;

    @NotBlank(message = "Airline name is mandatory")
    @Size(max = 160, message = "Airline name must not exceed 160 characters")
    private String name;

    @Size(max = 160, message = "Alias must not exceed 160 characters")
    private String alias;

    @NotBlank(message = "Country is mandatory")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 2048, message = "Logo URL must not exceed 2048 characters")
    private String logoUrl;

    @Size(max = 2048, message = "Website URL must not exceed 2048 characters")
    private String website;

    @Size(max = 100, message = "Alliance must not exceed 100 characters")
    private String alliance;

    @NotBlank(message = "Owner ID is mandatory")
    private Long ownerId;

    private Long headquartersCityId;

    @Email(message = "Support email must be a valid email address")
    @Size(max = 254, message = "Support email must not exceed 254 characters")
    private String supportEmail;

    @Size(max = 40, message = "Support phone must not exceed 40 characters")
    private String supportPhone;

    @Size(max = 200, message = "Support hours must not exceed 200 characters")
    private String supportHours;
}
