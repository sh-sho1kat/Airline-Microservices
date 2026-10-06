package com.sho1kat.payload.userservicedto.request.user;

import com.sho1kat.enums.UserRole;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoleRequest {

    @NotEmpty(message = "At least one role is required")
    private Set<UserRole> roles;
}


