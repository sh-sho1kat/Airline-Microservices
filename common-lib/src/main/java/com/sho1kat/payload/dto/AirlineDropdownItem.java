package com.sho1kat.payload.dto;

import lombok.*;

/** Light projection for select boxes. The all-args constructor is used by a JPQL "new" expression. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirlineDropdownItem {
    private Long id;
    private String name;
    private String iataCode;
    private String icaoCode;
    private String logoUrl;
    private String country;
}
