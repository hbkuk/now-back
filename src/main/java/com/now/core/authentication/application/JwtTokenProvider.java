package com.now.core.authentication.application;

import com.now.common.exception.ErrorType;
import com.now.core.authentication.application.dto.jwtTokens;
import com.now.core.authentication.application.dto.TokenClaims;
import com.now.core.authentication.exception.InvalidAuthenticationException;
import com.now.core.authentication.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * JWT(Json Web Token) 생성 및 검증을 담당하는 객체
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    public static final String BEARER_PREFIX = "Bearer";
    private static final String BEARER_PREFIX_WITH_SPACE = "Bearer ";

    private static final int ACCESS_TOKEN_EXPIRE_MINUTES = 10;
    private static final int REFRESH_TOKEN_EXPIRE_DAYS = 7;

    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";

    @Value("${now.security.key}")
    private String securityKey;

    /**
     * 전달받은 키-값 쌍을 기반으로 JWT Access 토큰을 생성 후 반환
     *
     * @param claims 토큰에 담을 클레임 정보를 포함한 Map 객체
     * @return JWT 토큰을 생성 후 반환
     */
    public String createAccessToken(TokenClaims claims) {
        return buildAccessToken(claims.getClaims());
    }

    /**
     * 전달받은 키-값 쌍을 기반으로 JWT 토큰을 생성 후 반환
     *
     * @return JWT 토큰을 생성 후 반환
     */
    public String createRefreshToken(TokenClaims claims) {
        return buildRefreshToken(claims.getClaims());
    }

    /**
     * 전달받은 토큰을 검증하고, 전달받은 키에 해당하는 클레임 값을 반환
     *
     * @param token 검증할 JWT 토큰
     * @param key   가져올 클레임의 키
     * @return 전달받은 키에 해당하는 클레임 값
     */
    public Object getClaim(String token, String key) {
        if (token == null) {
            throw new InvalidAuthenticationException(ErrorType.NOT_AUTHENTICATED);
        }

        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(Base64.getEncoder().encodeToString(securityKey.getBytes()))
                    .parseClaimsJws(removeBearer(token))
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(ErrorType.EXPIRED_ACCESS_TOKEN);
        } catch (JwtException e) {
            throw new InvalidTokenException(ErrorType.INVALID_TOKEN);
        }
        return claims.get(key);
    }

    /**
     * 주어진 token을 기반으로 Claims 반환
     *
     * @param token 토큰
     * @return Claims 반환
     */
    public Claims getAllClaims(String token) {
        if (token == null) {
            throw new InvalidAuthenticationException(ErrorType.NOT_AUTHENTICATED);
        }

        try {
            return Jwts.parser()
                    .setSigningKey(Base64.getEncoder().encodeToString(securityKey.getBytes()))
                    .parseClaimsJws(removeBearer(token))
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(ErrorType.EXPIRED_ACCESS_TOKEN);
        } catch (JwtException e) {
            throw new InvalidTokenException(ErrorType.INVALID_TOKEN);
        }
    }

    /**
     * 전달받은 토큰의 접두사를 붙인 토큰 반환
     *
     * @param token 원본 토큰
     * @return Bearer 접두사가 붙은 토큰
     */
    private String addBearerPrefix(String token) {
        return BEARER_PREFIX_WITH_SPACE + token;
    }

    /**
     * 전달받은 토큰의 접두사를 제거한 토큰 반환
     *
     * @param token Bearer 접두사가 포함된 토큰
     * @return Bearer 접두사가 제거된 토큰
     */
    private String removeBearer(String token) {
        return token.substring(BEARER_PREFIX_WITH_SPACE.length());
    }

    /**
     * 지정된 키-값 쌍을 기반으로 JWT 토큰을 생성 후 반환
     *
     * @param claims 토큰에 담을 클레임 정보를 포함한 Map 객체
     * @return JWT 토큰을 생성 후 반환
     */
    private String buildAccessToken(Map<String, Object> claims) {
        String token = buildToken(claims, ACCESS_TOKEN_EXPIRE_MINUTES, ChronoUnit.MINUTES);
        return addBearerPrefix(token);
    }

    /**
     * 지정된 키-값 쌍을 기반으로 JWT 토큰을 생성 후 반환
     *
     * @param claims 토큰에 담을 클레임 정보를 포함한 Map 객체
     * @return JWT 토큰을 생성 후 반환
     */
    private String buildRefreshToken(Map<String, Object> claims) {
        String token = buildToken(claims, REFRESH_TOKEN_EXPIRE_DAYS, ChronoUnit.DAYS);
        return addBearerPrefix(token);
    }

    /**
     * 주어진 키-값 쌍을 기반으로 JWT 토큰을 생성
     *
     * @param claims                  토큰에 담을 클레임 정보를 포함한 Map 객체
     * @param refreshTokenExpireHours Refresh 토큰의 유효 기간 (시간 단위)
     * @param chronoUnit                   refreshTokenExpireHours 매개변수의 시간 단위 (예: ChronoUnit.HOURS)
     * @return 생성된 JWT 토큰
     */
    private String buildToken(Map<String, Object> claims, int refreshTokenExpireHours, ChronoUnit chronoUnit) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(Instant.now().plus(refreshTokenExpireHours, chronoUnit)))
                .signWith(SignatureAlgorithm.HS512, Base64.getEncoder().encodeToString(securityKey.getBytes()))
                .compact();
    }

    /**
     * 주어진 JWT 토큰의 유효기간이 만료되었다면 true 반환, 그렇지 않다면 false 반환
     *
     * @param token 토큰
     * @return 유효기간이 만료되었다면 true 반환, 그렇지 않다면 false 반환
     */
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(securityKey.getBytes()))
                .parseClaimsJws(removeBearer(token))
                .getBody();
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            throw new InvalidTokenException(ErrorType.INVALID_TOKEN);
        }
        return false;
    }

    /**
     * 새로운 Access Token과 Refresh Token을 반환
     *
     * @param  accessToken 액세스 토큰
     * @return 새로 발급된 Access Token과 Refresh Token 정보를 담은 Token 객체
     */
    public jwtTokens refreshTokens(String accessToken) {
        return jwtTokens.builder()
                .accessToken(createAccessToken(TokenClaims.create(getAllClaims(accessToken))))
                .refreshToken(createRefreshToken(TokenClaims.create(getAllClaims(accessToken))))
                .build();
    }

    /**
     *Refresh를 위한 토큰 검증을 수행
     *
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     */
    public void validateForRefresh(String accessToken, String refreshToken) {
        if (accessToken == null || refreshToken == null) {
            throw new InvalidAuthenticationException(ErrorType.NOT_AUTHENTICATED);
        }
        if (isTokenExpired(refreshToken)) {
            throw new InvalidAuthenticationException(ErrorType.EXPIRED_REFRESH_TOKEN);
        }
    }
}
