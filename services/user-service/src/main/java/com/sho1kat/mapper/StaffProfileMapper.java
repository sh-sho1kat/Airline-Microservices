package com.sho1kat.mapper;

import com.sho1kat.entity.StaffProfile;
import com.sho1kat.payload.userservicedto.request.auth.StaffSignUpRequest;
import com.sho1kat.payload.userservicedto.response.StaffProfileResponse;

public class StaffProfileMapper {

    private StaffProfileMapper() {
    }

    public static StaffProfile toEntity(
            StaffSignUpRequest request
    ) {
        if (request == null) {
            return null;
        }

        return StaffProfile.builder()
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .jobTitle(request.getJobTitle())
                .address(request.getAddress())
                .build();
    }

    public static StaffProfileResponse toResponse(
            StaffProfile staffProfile
    ) {
        if (staffProfile == null) {
            return null;
        }

        return StaffProfileResponse.builder()
                .id(staffProfile.getId())
                .userResponse(
                        UserMapper.toResponse(
                                staffProfile.getUser()
                        )
                )
                .employeeId(staffProfile.getEmployeeId())
                .department(staffProfile.getDepartment())
                .jobTitle(staffProfile.getJobTitle())
                .address(
                        AddressMapper.toResponse(
                                staffProfile.getAddress()
                        )
                )
                .approvedAt(staffProfile.getApprovedAt())
                .approvedBy(staffProfile.getApprovedBy())
                .rejectionReason(
                        staffProfile.getRejectionReason()
                )
                .createdAt(staffProfile.getCreatedAt())
                .build();
    }
}