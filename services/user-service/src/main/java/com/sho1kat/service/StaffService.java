package com.sho1kat.service;

import com.sho1kat.payload.userservicedto.request.user.RejectStaffRequest;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;

import java.util.UUID;

public interface StaffService {

    UserResponse approveStaff(
            UUID userId,
            String adminEmail
    );

    MessageResponse rejectStaff(
            UUID userId,
            RejectStaffRequest request,
            String adminEmail
    );
}