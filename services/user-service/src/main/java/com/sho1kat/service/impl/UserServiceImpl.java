package com.sho1kat.service.impl;

import com.sho1kat.entity.User;
import com.sho1kat.enums.UserRole;
import com.sho1kat.mapper.UserMapper;
import com.sho1kat.payload.userservicedto.request.user.UpdateRoleRequest;
import com.sho1kat.payload.userservicedto.request.user.UpdateStatusRequest;
import com.sho1kat.payload.userservicedto.request.user.UpdateUserRequest;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;
import com.sho1kat.repository.UserRepository;
import com.sho1kat.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUser(String email) {

        User user = getUserByEmail(email);

        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponse updateCurrentUser(
            String email,
            UpdateUserRequest request
    ) {

        User user = getUserByEmail(email);

        UserMapper.updateEntity(user, request);

        User updatedUser = userRepository.save(user);

        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse updateRole(
            Long userId,
            UpdateRoleRequest request
    ) {

        User user = getUserById(userId);

        if (request.getRoles() == null ||
                request.getRoles().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one role is required"
            );
        }

        /*
         * STAFF role can only be assigned if the user
         * has an approved staff profile.
         */
        if (request.getRoles().contains(UserRole.STAFF)) {

            if (user.getStaffProfile() == null) {
                throw new IllegalStateException(
                        "User does not have a staff profile"
                );
            }

            if (user.getStaffProfile().getApprovedAt() == null) {
                throw new IllegalStateException(
                        "Staff profile has not been approved"
                );
            }
        }

        user.setRoles(
                new HashSet<>(request.getRoles())
        );

        User updatedUser = userRepository.save(user);

        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse updateStatus(
            Long userId,
            UpdateStatusRequest request
    ) {

        User user = getUserById(userId);

        user.setStatus(request.getStatus());

        User updatedUser = userRepository.save(user);

        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public MessageResponse deleteUser(Long userId) {

        User user = getUserById(userId);

        userRepository.delete(user);

        return new MessageResponse(
                "User deleted successfully"
        );
    }

    private User getUserByEmail(String email) {

        User user = userRepository.findByEmail(
                email.trim().toLowerCase()
        );

        if (user == null) {
            throw new IllegalArgumentException(
                    "User not found with email: " + email
            );
        }

        return user;
    }

    private User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: " + userId
                        )
                );
    }
}