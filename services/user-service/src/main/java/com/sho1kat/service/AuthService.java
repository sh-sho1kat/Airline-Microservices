package com.sho1kat.service;

import com.sho1kat.payload.auth.response.AuthResponse;
import com.sho1kat.payload.dto.UserDto;

public interface AuthService {
    AuthResponse login(String username, String password);
    AuthResponse signup(UserDto userDto);
}
