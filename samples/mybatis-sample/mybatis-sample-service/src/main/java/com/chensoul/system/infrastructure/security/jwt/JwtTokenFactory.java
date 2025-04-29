/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chensoul.system.infrastructure.security.jwt;

import com.chensoul.json.JacksonUtils;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.setting.domain.JwtSetting;
import com.chensoul.system.domain.setting.domain.SystemSetting;
import com.chensoul.system.domain.setting.domain.SystemSettingType;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.chensoul.system.infrastructure.security.jwt.token.AccessJwtToken;
import com.chensoul.system.infrastructure.security.jwt.token.JwtPair;
import com.chensoul.system.infrastructure.security.jwt.token.JwtToken;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.infrastructure.security.util.UserPrincipal;
import com.chensoul.system.user.domain.Authority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFactory {
    private static final String SCOPES = "scopes";
    private static final String USER_ID = "userId";
    private static final String NAME = "name";
    private static final String ENABLED = "enabled";
    private static final String IS_PUBLIC = "isPublic";
    private static final String TENANT_ID = "tenantId";
    private static final String MERCHANT_ID = "merchantId";
    private static final String SESSION_ID = "sessionId";

    private final SystemSettingService systemSettingService;
    private final TokenCacheService tokenCacheService;

    /**
     * Factory method for issuing new JWT Tokens.
     */
    public AccessJwtToken createAccessJwtToken(SecurityUser securityUser) {
        if (securityUser.getAuthority() == null) {
            throw new IllegalArgumentException("User doesn't have any privileges");
        }

        SystemSetting systemSetting = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.JWT);
        JwtSetting jwtSetting = JacksonUtils.convertValue(systemSetting.getExtra(), JwtSetting.class);
        JwtBuilder jwtBuilder = setUpToken(securityUser, securityUser.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).collect(Collectors.toList()), jwtSetting.getTokenExpirationTime());
        jwtBuilder.claim(NAME, securityUser.getName())
            .claim(ENABLED, securityUser.isEnabled())
            .claim(IS_PUBLIC, securityUser.getUserPrincipal().getType() == UserPrincipal.Type.PUBLIC_ID);

        if (StringUtils.isNotBlank(securityUser.getTenantId())) {
            jwtBuilder.claim(TENANT_ID, securityUser.getTenantId());
        }
        if (securityUser.getMerchantId() != null) {
            jwtBuilder.claim(MERCHANT_ID, securityUser.getMerchantId());
        }

        String token = jwtBuilder.compact();
        return new AccessJwtToken(token);
    }

    public SecurityUser parseAccessJwtToken(String token) {
        Jws<Claims> jwsClaims = parseTokenClaims(token);
        Claims claims = jwsClaims.getBody();
        String subject = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> scopes = claims.get(SCOPES, List.class);
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("JWT Token doesn't have any scopes");
        }

        SecurityUser securityUser = new SecurityUser();
        securityUser.setId(claims.get(USER_ID, Long.class));
        securityUser.setEmail(subject);
        securityUser.setAuthority(Authority.parse(scopes.get(0)));
        String tenantId = claims.get(TENANT_ID, String.class);
        if (StringUtils.isNotBlank(tenantId)) {
            securityUser.setTenantId(tenantId);
        } else if (securityUser.getAuthority() == Authority.SYS_ADMIN) {
            securityUser.setTenantId(SYS_TENANT_ID);
        }
        Long merchantId = claims.get(MERCHANT_ID, Long.class);
        if (merchantId != null) {
            securityUser.setMerchantId(merchantId);
        }
        if (claims.get(SESSION_ID, String.class) != null) {
            securityUser.setSessionId(claims.get(SESSION_ID, String.class));
        }

        if (tokenCacheService.isExpired(securityUser.getId(), securityUser.getSessionId(), claims.getIssuedAt().getTime())) {
            throw new JwtExpiredTokenException("Token has expired");
        }

        UserPrincipal principal;
        if (securityUser.getAuthority() != Authority.PRE_VERIFICATION_TOKEN) {
            securityUser.setName(claims.get(NAME, String.class));
            securityUser.setEnabled(claims.get(ENABLED, Boolean.class));
            boolean isPublic = claims.get(IS_PUBLIC, Boolean.class);
            principal = new UserPrincipal(isPublic ? UserPrincipal.Type.PUBLIC_ID : UserPrincipal.Type.USER_NAME, subject);
        } else {
            principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, subject);
        }
        securityUser.setUserPrincipal(principal);

        return securityUser;
    }

    public JwtToken createRefreshToken(SecurityUser securityUser) {
        UserPrincipal principal = securityUser.getUserPrincipal();
        SystemSetting systemSetting = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.JWT);
        JwtSetting jwtSetting = JacksonUtils.convertValue(systemSetting.getExtra(), JwtSetting.class);
        String token = setUpToken(securityUser, Collections.singletonList(Authority.REFRESH_TOKEN.name()), jwtSetting.getRefreshTokenExpTime())
            .claim(IS_PUBLIC, principal.getType() == UserPrincipal.Type.PUBLIC_ID)
            .setId(UUID.randomUUID().toString()).compact();

        return new AccessJwtToken(token);
    }

    public SecurityUser parseRefreshToken(String token) {
        Jws<Claims> jwsClaims = parseTokenClaims(token);
        Claims claims = jwsClaims.getBody();
        String subject = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> scopes = claims.get(SCOPES, List.class);
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("Refresh Token doesn't have any scopes");
        }
        if (!scopes.get(0).equals(Authority.REFRESH_TOKEN.name())) {
            throw new IllegalArgumentException("Invalid Refresh Token scope");
        }
        boolean isPublic = claims.get(IS_PUBLIC, Boolean.class);
        UserPrincipal principal = new UserPrincipal(isPublic ? UserPrincipal.Type.PUBLIC_ID : UserPrincipal.Type.USER_NAME, subject);
        SecurityUser securityUser = new SecurityUser();
        securityUser.setId(claims.get(USER_ID, Long.class));
        securityUser.setUserPrincipal(principal);
        if (claims.get(SESSION_ID, String.class) != null) {
            securityUser.setSessionId(claims.get(SESSION_ID, String.class));
        }

        if (tokenCacheService.isExpired(securityUser.getId(), securityUser.getSessionId(), claims.getIssuedAt().getTime())) {
            throw new JwtExpiredTokenException("Token has expired");
        }

        return securityUser;
    }

    public JwtToken createPreVerificationToken(SecurityUser user, Integer expirationTime) {
        JwtBuilder jwtBuilder = setUpToken(user, Collections.singletonList(Authority.PRE_VERIFICATION_TOKEN.name()), expirationTime)
            .claim(TENANT_ID, user.getTenantId());
        if (user.getMerchantId() != null) {
            jwtBuilder.claim(MERCHANT_ID, user.getMerchantId());
        }
        return new AccessJwtToken(jwtBuilder.compact());
    }

    private JwtBuilder setUpToken(SecurityUser securityUser, List<String> scopes, long expirationTime) {
        if (StringUtils.isBlank(securityUser.getEmail())) {
            throw new IllegalArgumentException("Cannot create JWT Token without username/email");
        }

        UserPrincipal principal = securityUser.getUserPrincipal();

        Claims claims = Jwts.claims().setSubject(principal.getValue());
        claims.put(USER_ID, securityUser.getId());
        claims.put(SCOPES, scopes);
        if (securityUser.getSessionId() != null) {
            claims.put(SESSION_ID, securityUser.getSessionId());
        }

        ZonedDateTime currentTime = ZonedDateTime.now();

        SystemSetting systemSetting = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.JWT);
        JwtSetting jwtSetting = JacksonUtils.convertValue(systemSetting.getExtra(), JwtSetting.class);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuer(jwtSetting.getTokenIssuer())
            .setIssuedAt(Date.from(currentTime.toInstant()))
            .setExpiration(Date.from(currentTime.plusSeconds(expirationTime).toInstant()))
            .signWith(SignatureAlgorithm.HS512, jwtSetting.getTokenSigningKey());
    }

    public Jws<Claims> parseTokenClaims(String token) {
        SystemSetting systemSetting = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.JWT);
        JwtSetting jwtSetting = JacksonUtils.convertValue(systemSetting.getExtra(), JwtSetting.class);

        try {
            return Jwts.parser().setSigningKey(jwtSetting.getTokenSigningKey()).parseClaimsJws(token);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Token has Invalid", ex);
        } catch (ExpiredJwtException expiredEx) {
            throw new JwtExpiredTokenException(token, "Token has expired", expiredEx);
        }
    }

    public JwtPair createTokenPair(SecurityUser securityUser) {
        JwtToken accessToken = createAccessJwtToken(securityUser);
        JwtToken refreshToken = createRefreshToken(securityUser);
        return new JwtPair(accessToken.getToken(), refreshToken.getToken(), securityUser.getAuthority());
    }

}
