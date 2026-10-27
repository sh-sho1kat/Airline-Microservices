package com.sho1kat.service;

import com.sho1kat.payload.pricingServiceDto.request.FlightInstanceSyncRequest;
import com.sho1kat.payload.pricingServiceDto.response.FlightInstanceSyncResponse;

public interface FlightInstanceReferenceService {

    // Idempotent upsert of the flight-ops read model; templates apply only on first registration.
    FlightInstanceSyncResponse upsertFromFlightOps(FlightInstanceSyncRequest syncRequest);
}
