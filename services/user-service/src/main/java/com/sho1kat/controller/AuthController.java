package com.sho1kat.controller;

import com.sho1kat.payload.userservicedto.request.auth.*;
import com.sho1kat.payload.userservicedto.response.AuthResponse;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;
import com.sho1kat.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody UserSignUpRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.signup(request));
    }

    @PostMapping("/register-staff")
    public ResponseEntity<MessageResponse> registerStaff(@Valid @RequestBody StaffSignUpRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerStaff(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LogInRequest request) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(
                authService.forgotPassword(request)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(
                authService.resetPassword(request)
        );
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                authService.changePassword(email, request)
        );
    }
}