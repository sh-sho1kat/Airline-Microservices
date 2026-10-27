package com.sho1kat.payload.pricingServiceDto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInstanceSyncResponse {

    private Long flightInstanceId;

    // Fares generated from templates; only applied when the instance is first registered.
    private Integer fareTemplatesApplied;

    // True when the instance was not known to pricing before this sync.
    private Boolean referenceCreated;
}
