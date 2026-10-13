package com.sho1kat.payload.airlineServiceDto.response;

import com.sho1kat.embeddable.Support;
import com.sho1kat.enums.AirlineStatus;
import com.sho1kat.payload.response.CityResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirlineResponse {

    private Long id;

    private String iataCode;
    private String icaoCode;

    private String name;
    private String alias;

    private String country;
    private String logoUrl;
    private String website;

    private AirlineStatus status;
    private String alliance;

    private Long headquartersCityId;
    private CityResponse headquartersCity;

    private Support support;

    private Instant createdAt;
    private Instant updatedAt;

    private Long ownerId;
    private UserResponse owner;
    private Long updatedById;
}

