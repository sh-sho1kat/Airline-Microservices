package com.sho1kat.service;


import com.sho1kat.payload.userservicedto.request.user.UpdateRoleRequest;
import com.sho1kat.payload.userservicedto.request.user.UpdateStatusRequest;
import com.sho1kat.payload.userservicedto.request.user.UpdateUserRequest;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getCurrentUser(String email);
    UserResponse updateCurrentUser(String email, UpdateUserRequest request);
    UserResponse updateRole(UUID userId, UpdateRoleRequest request);
    UserResponse updateStatus(UUID userId, UpdateStatusRequest request);
    MessageResponse deleteUser(UUID userId);
}