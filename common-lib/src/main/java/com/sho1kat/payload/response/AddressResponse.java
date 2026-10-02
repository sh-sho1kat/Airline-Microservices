package com.sho1kat.payload.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private String addressLine;

    private String postalCode;

    private String district;

    private String country;
}