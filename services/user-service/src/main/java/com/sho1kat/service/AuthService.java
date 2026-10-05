package com.sho1kat.service;

import com.sho1kat.payload.auth.response.AuthResponse;
import com.sho1kat.payload.dto.UserDto;

public interface AuthService {
    AuthResponse logIn(String username, String password);
    AuthResponse signUp(UserDto userDto);
}
