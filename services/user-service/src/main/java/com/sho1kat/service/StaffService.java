package com.sho1kat.service;

import com.sho1kat.payload.userservicedto.request.user.RejectStaffRequest;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;

public interface StaffService {

    UserResponse approveStaff(
            Long userId,
            String adminEmail
    );

    MessageResponse rejectStaff(
            Long userId,
            RejectStaffRequest request,
            String adminEmail
    );
}