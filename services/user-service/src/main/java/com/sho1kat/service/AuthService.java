package com.sho1kat.service;


import com.sho1kat.payload.userservicedto.request.auth.*;
import com.sho1kat.payload.userservicedto.response.AuthResponse;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;

public interface AuthService {

    AuthResponse signup(UserSignUpRequest request);

    MessageResponse registerStaff(StaffSignUpRequest request);

    AuthResponse login(LogInRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    MessageResponse logout(LogoutRequest request);

    MessageResponse resendVerification(EmailRequest request);

    MessageResponse forgotPassword(EmailRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MessageResponse changePassword(String email, ChangePasswordRequest request);

    MessageResponse verifyEmail(VerifyEmailRequest request);
}