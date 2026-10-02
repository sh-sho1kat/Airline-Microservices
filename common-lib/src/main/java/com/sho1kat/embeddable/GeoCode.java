package com.sho1kat.embeddable;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoCode {

    private Double latitude;
    private Double longitude;
}