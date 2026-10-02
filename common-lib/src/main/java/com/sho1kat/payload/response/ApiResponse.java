package com.sho1kat.payload.response;


import lombok.Data;

@Data
public class ApiResponse<A> {
    private String message;
}
