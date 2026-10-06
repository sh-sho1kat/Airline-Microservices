package com.sho1kat.mapper;

import com.sho1kat.entity.User;
import com.sho1kat.payload.userservicedto.request.auth.UserSignUpRequest;
import com.sho1kat.payload.userservicedto.request.user.UpdateUserRequest;
import com.sho1kat.payload.userservicedto.response.UserResponse;

public class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(
            UserSignUpRequest request,
            String encodedPassword
    ) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .build();
    }

    public static void updateEntity(
            User user,
            UpdateUserRequest request
    ) {
        if (user == null || request == null) {
            return;
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
    }

    public static UserResponse toResponse(
            User user
    ) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}