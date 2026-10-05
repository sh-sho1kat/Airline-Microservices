package com.sho1kat.payload.auth.request;


import lombok.Data;

@Data
public class LogInRequest {
    private String email;
    private String password;
}
