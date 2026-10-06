package com.sho1kat.controller;

import com.sho1kat.payload.userservicedto.request.user.RejectStaffRequest;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;
import com.sho1kat.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> approveStaff(@PathVariable UUID userId, Authentication authentication) {
        String adminEmail = authentication.getName();

        return ResponseEntity.ok(
                staffService.approveStaff(
                        userId,
                        adminEmail
                )
        );
    }

    @PostMapping("/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> rejectStaff(@PathVariable UUID userId, Authentication authentication, @Valid @RequestBody RejectStaffRequest request) {
        String adminEmail = authentication.getName();

        return ResponseEntity.ok(
                staffService.rejectStaff(
                        userId,
                        request,
                        adminEmail
                )
        );
    }
}