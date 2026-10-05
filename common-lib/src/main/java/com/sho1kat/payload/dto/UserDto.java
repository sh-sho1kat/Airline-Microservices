package com.sho1kat.payload.dto;

import com.sho1kat.enums.UserRole;
import com.sho1kat.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Set<UserRole> userRoles;
    private UserStatus userStatus;
    private Instant createdAt;
    private Instant updatedAt;
    private String password;

}
