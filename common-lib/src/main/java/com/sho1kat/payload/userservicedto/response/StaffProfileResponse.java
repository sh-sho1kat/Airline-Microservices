package com.sho1kat.payload.userservicedto.response;

import com.sho1kat.payload.response.AddressResponse;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffProfileResponse {

    private Long id;

    private UserResponse userResponse;

    private String employeeId;

    private String department;

    private String jobTitle;

    private AddressResponse address;

    private Instant approvedAt;

    private Long approvedBy;

    private String rejectionReason;

    private Instant createdAt;
}