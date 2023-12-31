package com.now.core.authentication.application.dto;

import lombok.*;

/**
 * AccessToken, RefreshToken 정보를 담고있는 객체
 */
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class jwtTokens {
    private final String accessToken;
    private final String refreshToken;
}
