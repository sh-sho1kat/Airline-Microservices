package com.sho1kat.service.impl;

import com.sho1kat.entity.StaffProfile;
import com.sho1kat.entity.User;
import com.sho1kat.enums.UserRole;
import com.sho1kat.enums.UserStatus;
import com.sho1kat.mapper.UserMapper;
import com.sho1kat.payload.userservicedto.request.user.RejectStaffRequest;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;
import com.sho1kat.repository.UserRepository;
import com.sho1kat.service.MailService;
import com.sho1kat.service.StaffService;
import com.sho1kat.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final MailService mailService;

    @Override
    public UserResponse approveStaff(Long userId, String adminEmail) {
        User user = getUserById(userId);
        StaffProfile profile = requirePendingProfile(user);
        User admin = requireAdmin(adminEmail);

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("User has not verified their email");
        }
        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            user.setStatus(UserStatus.ACTIVE);
        } else if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Cannot approve a user with status " + user.getStatus());
        }

        profile.setApprovedAt(Instant.now());
        profile.setApprovedBy(admin.getId());
        profile.setRejectionReason(null);
        user.getRoles().add(UserRole.STAFF);

        mailService.sendStaffApprovedEmail(
                user.getEmail(), user.getFirstName() + " " + user.getLastName());

        return UserMapper.toResponse(user);
    }

    @Override
    public MessageResponse rejectStaff(Long userId, RejectStaffRequest request, String adminEmail) {
        User user = getUserById(userId);
        StaffProfile profile = requirePendingProfile(user);
        User admin = requireAdmin(adminEmail);

        String reason = request.getRejectionReason();
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }

        profile.setRejectionReason(reason.trim());
        profile.setApprovedAt(null);
        profile.setApprovedBy(admin.getId()); // means "decided by" when approvedAt is null

        user.getRoles().remove(UserRole.STAFF);
        if (user.getRoles().isEmpty()) {
            user.getRoles().add(UserRole.USER);
        }

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            user.setStatus(UserStatus.REJECTED);
            tokenService.revokeAllRefreshTokens(user);
        }

        mailService.sendStaffRejectedEmail(user.getEmail(), user.getFirstName(), reason.trim());

        return new MessageResponse("Staff registration rejected successfully");
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

    private StaffProfile requirePendingProfile(User user) {
        StaffProfile profile = user.getStaffProfile();
        if (profile == null) {
            throw new IllegalStateException("User does not have a staff profile");
        }
        if (profile.getApprovedAt() != null) {
            throw new IllegalStateException("Staff profile is already approved");
        }
        if (profile.getRejectionReason() != null) {
            throw new IllegalStateException("Staff profile was already rejected");
        }
        return profile;
    }

    private User requireAdmin(String email) {
        User admin = getUserByEmail(email);
        if (!admin.getRoles().contains(UserRole.ADMIN)) {
            throw new AccessDeniedException("Admin role required");
        }
        return admin;
    }
}