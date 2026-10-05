package com.sho1kat.controller;


import com.sho1kat.payload.auth.request.LogInRequest;
import com.sho1kat.payload.auth.response.AuthResponse;
import com.sho1kat.payload.dto.UserDto;
import com.sho1kat.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody UserDto userDto) throws Exception {
        AuthResponse authResponse = authService.signUp(userDto);

        return  ResponseEntity.ok(authResponse);

    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> logIn(@RequestBody @Valid LogInRequest logInRequest) throws Exception {
        AuthResponse authResponse = authService.logIn(logInRequest.getEmail(), logInRequest.getPassword());
        return  ResponseEntity.ok(authResponse);
    }
}
