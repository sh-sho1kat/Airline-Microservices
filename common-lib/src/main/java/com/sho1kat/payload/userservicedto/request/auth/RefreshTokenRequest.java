package com.sho1kat.payload.userservicedto.request.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
