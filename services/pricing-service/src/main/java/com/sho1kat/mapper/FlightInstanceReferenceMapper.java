package com.sho1kat.mapper;

import com.sho1kat.entity.FlightInstanceReference;
import com.sho1kat.payload.pricingServiceDto.request.FlightInstanceSyncRequest;
import com.sho1kat.payload.pricingServiceDto.response.UnpricedFlightResponse;
import org.springframework.stereotype.Component;

@Component
public class FlightInstanceReferenceMapper {

    public FlightInstanceReference toEntity(FlightInstanceSyncRequest request) {

        FlightInstanceReference reference = new FlightInstanceReference();

        reference.setFlightInstanceId(request.getFlightInstanceId());
        reference.setFlightId(request.getFlightId());
        reference.setAirlineId(request.getAirlineId());
        reference.setDepartureAt(request.getDepartureAt());
        reference.setIsActive(request.getIsActive() == null || request.getIsActive());

        return reference;
    }

    public void updateEntity(FlightInstanceSyncRequest request, FlightInstanceReference existing) {

        if (request == null || existing == null) {
            return;
        }

        existing.setFlightId(request.getFlightId());
        existing.setAirlineId(request.getAirlineId());
        existing.setDepartureAt(request.getDepartureAt());

        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        // flightInstanceId is primary key and immutable.
    }

    public UnpricedFlightResponse toUnpricedResponse(FlightInstanceReference reference) {

        if (reference == null) {
            return null;
        }

        return UnpricedFlightResponse.builder()
                .flightInstanceId(reference.getFlightInstanceId())
                .flightId(reference.getFlightId())
                .airlineId(reference.getAirlineId())
                .departureAt(reference.getDepartureAt())
                .build();
    }
}
