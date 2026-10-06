package com.sho1kat.payload.userservicedto.response;

import com.sho1kat.enums.Gender;
import com.sho1kat.enums.UserRole;
import com.sho1kat.enums.UserStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;

    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    private Gender gender;

    private UserStatus status;

    private boolean emailVerified;

    private Set<UserRole> roles;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant lastLogin;
}