package com.sho1kat.payload.response;
import lombok.*;

import java.time.ZoneId;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirportResponse {

    private Long id;

    private String iataCode;

    private String name;

    private String timeZoneId;

    private AddressResponse address;

    private GeoCodeResponse geoCode;

    private CityResponse cityResponse;
}