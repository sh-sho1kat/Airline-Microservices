package com.sho1kat.payload.userservicedto.request.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LogInRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
}
