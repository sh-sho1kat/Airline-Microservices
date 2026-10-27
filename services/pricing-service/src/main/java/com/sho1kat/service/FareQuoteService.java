package com.sho1kat.service;

import com.sho1kat.enums.CabinClass;
import com.sho1kat.payload.pricingServiceDto.response.QuoteResponse;

public interface FareQuoteService {

    QuoteResponse quoteFare(Long flightInstanceId, CabinClass cabin, int adults, int children,
                            int infants, String fareClass, String couponCode);
}
