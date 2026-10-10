package com.sho1kat.controller;

import com.sho1kat.payload.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping
    public ApiResponse home() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Hello From Airline Core Service");
        return apiResponse;
    }
}
