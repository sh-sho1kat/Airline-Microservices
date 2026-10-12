package com.sho1kat.payload.airlineServiceDto.response;


import com.sho1kat.enums.AircraftStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AircraftStatusResponse {
    private String aircraftCode;
    private String airlineName;
    private AircraftStatus status;
    private Boolean isAvailable;
    private Boolean operational;
    private Boolean requiresMaintenance;
    private LocalDate nextMaintenanceDate;

}
