package com.sho1kat.controller;

import com.sho1kat.payload.userservicedto.request.user.UpdateRoleRequest;
import com.sho1kat.payload.userservicedto.request.user.UpdateStatusRequest;
import com.sho1kat.payload.userservicedto.request.user.UpdateUserRequest;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;
import com.sho1kat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                userService.getCurrentUser(email)
        );
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateCurrentUser(Authentication authentication, @Valid @RequestBody UpdateUserRequest request) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                userService.updateCurrentUser(email, request)
        );
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long userId, @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateRole(userId, request)
        );
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable Long userId, @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(
                userService.updateStatus(userId, request)
        );
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                userService.deleteUser(userId)
        );
    }
}