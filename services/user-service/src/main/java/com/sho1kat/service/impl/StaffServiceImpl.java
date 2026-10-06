package com.sho1kat.service.impl;

import com.sho1kat.entity.StaffProfile;
import com.sho1kat.entity.User;
import com.sho1kat.enums.UserRole;
import com.sho1kat.mapper.UserMapper;
import com.sho1kat.payload.userservicedto.request.user.RejectStaffRequest;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;
import com.sho1kat.repository.UserRepository;
import com.sho1kat.service.StaffService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;

    @Override
    public UserResponse approveStaff(
            Long userId,
            String adminEmail
    ) {

        User user = getUserById(userId);

        StaffProfile staffProfile =
                user.getStaffProfile();

        if (staffProfile == null) {
            throw new IllegalStateException(
                    "User does not have a staff profile"
            );
        }

        if (staffProfile.getApprovedAt() != null) {
            throw new IllegalStateException(
                    "Staff profile is already approved"
            );
        }

        User admin = getUserByEmail(adminEmail);

        /*
         * Approve the staff application.
         */
        staffProfile.setApprovedAt(
                Instant.now()
        );

        staffProfile.setApprovedBy(
                admin.getId()
        );

        staffProfile.setRejectionReason(null);

        /*
         * Preserve existing roles and add STAFF.
         *
         * Example:
         * [USER] -> [USER, STAFF]
         */
        Set<UserRole> roles =
                new HashSet<>(user.getRoles());

        roles.add(UserRole.STAFF);

        user.setRoles(roles);

        User savedUser =
                userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    @Override
    public MessageResponse rejectStaff(
            Long userId,
            RejectStaffRequest request,
            String adminEmail
    ) {

        User user = getUserById(userId);

        StaffProfile staffProfile =
                user.getStaffProfile();

        if (staffProfile == null) {
            throw new IllegalStateException(
                    "User does not have a staff profile"
            );
        }

        if (staffProfile.getApprovedAt() != null) {
            throw new IllegalStateException(
                    "Approved staff cannot be rejected"
            );
        }

        /*
         * Verify that the request is being handled by
         * an existing admin.
         */
        getUserByEmail(adminEmail);

        staffProfile.setRejectionReason(
                request.getRejectionReason()
        );

        staffProfile.setApprovedAt(null);
        staffProfile.setApprovedBy(null);

        /*
         * Make sure rejected staff does not have STAFF
         * authority.
         */
        Set<UserRole> roles =
                new HashSet<>(user.getRoles());

        roles.remove(UserRole.STAFF);

        /*
         * Keep the normal USER role.
         */
        if (roles.isEmpty()) {
            roles.add(UserRole.USER);
        }

        user.setRoles(roles);

        userRepository.save(user);

        return new MessageResponse(
                "Staff registration rejected successfully"
        );
    }

    private User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: " + userId
                        )
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
}