package com.sho1kat.payload.userservicedto.request.auth;

import com.sho1kat.embeddable.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class StaffSignUpRequest {

    @Valid
    @NotNull(message = "User information is required")
    private UserSignUpRequest user;

    @NotBlank(message = "Employee ID is required")
    @Size(max = 50, message = "Employee ID cannot exceed 50 characters")
    private String employeeId;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title cannot exceed 100 characters")
    private String jobTitle;

    @Valid
    @NotNull(message = "Address is required")
    private Address address;
}

//{
//        "user": {
//        "email": "staff@gmail.com",
//        "password": "Password@123",
//        "firstName": "Shefat",
//        "lastName": "Hossen",
//        "phoneNumber": "+8801712345678",
//        "dateOfBirth": "2000-05-15",
//        "gender": "MALE"
//        },
//        "employeeId": "EMP-1001",
//        "department": "Flight Operations",
//        "jobTitle": "Flight Operations Officer",
//        "address": {
//        "addressLine": "Airport Road",
//        "postalCode": "1215",
//        "district": "Dhaka",
//        "country": "Bangladesh"
//        }
//}