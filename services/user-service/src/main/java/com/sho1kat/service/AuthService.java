package com.sho1kat.service;


import com.sho1kat.payload.userservicedto.request.auth.*;
import com.sho1kat.payload.userservicedto.response.AuthResponse;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;

public interface AuthService {

    AuthResponse signup(UserSignUpRequest request);

    UserResponse registerStaff(StaffSignUpRequest request);

    AuthResponse login(LogInRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MessageResponse changePassword(
            String email,
            ChangePasswordRequest request
    );
}