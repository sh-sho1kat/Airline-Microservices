package com.sho1kat.payload.userservicedto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String jwt;
    private String refreshToken;
    private String tokenType;
    private String message;
    private String title;
    private String status;
    private UserResponse user;
}
