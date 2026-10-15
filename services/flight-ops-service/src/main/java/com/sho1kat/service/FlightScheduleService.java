package com.sho1kat.service;

import com.sho1kat.payload.flightServiceDto.request.FlightScheduleRequest;
import com.sho1kat.payload.flightServiceDto.response.FlightScheduleResponse;

import java.util.List;

public interface FlightScheduleService {

    FlightScheduleResponse createFlightSchedule(
            Long airlineId,
            FlightScheduleRequest request
    );

    FlightScheduleResponse getFlightScheduleById(
            Long airlineId,
            Long scheduleId
    );

    List<FlightScheduleResponse> getFlightScheduleByAirline(
            Long airlineId
    );

    FlightScheduleResponse updateFlightSchedule(
            Long airlineId,
            Long scheduleId,
            FlightScheduleRequest request
    );

    void deleteFlightSchedule(
            Long airlineId,
            Long scheduleId
    );
}