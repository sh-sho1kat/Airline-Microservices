package com.sho1kat.payload.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    private String addressLine;

    private String postalCode;

    private String district;

    private String country;
}