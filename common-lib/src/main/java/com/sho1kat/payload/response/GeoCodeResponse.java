package com.sho1kat.payload.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoCodeResponse {

    private Double latitude;

    private Double longitude;
}