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
package com.chensoul.system.infrastructure.security.rest;

import com.chensoul.system.domain.setting.domain.SecuritySetting;
import com.chensoul.system.domain.setting.service.SecuritySettingService;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.chensoul.system.domain.user.service.UserService;
import com.chensoul.system.infrastructure.security.jwt.token.TwoFaAuthenticationToken;
import com.chensoul.system.infrastructure.security.mfa.MfaAuthService;
import com.chensoul.system.infrastructure.security.rest.exception.UserPasswordNotValidException;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.infrastructure.security.util.UserPrincipal;
import com.chensoul.system.user.domain.User;
import com.chensoul.system.user.domain.UserCredential;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Slf4j
@RequiredArgsConstructor
public class RestAuthenticationProvider implements AuthenticationProvider {

    private final SystemSettingService systemSettingService;
    private final SecuritySettingService securitySettingService;
    private final UserService userService;
    //    private final MerchantService merchantService;
    private final MfaAuthService mfaAuthService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.notNull(authentication, "No authentication data provided");

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new BadCredentialsException("Authentication Failed. Bad user principal.");
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        SecurityUser securityUser = null;
        if (userPrincipal.getType() == UserPrincipal.Type.USER_NAME) {
            String username = userPrincipal.getValue();
            String password = (String) authentication.getCredentials();

            SecuritySetting.PasswordPolicy passwordPolicy = systemSettingService.getSecuritySetting().getPasswordPolicy();
            if (Boolean.TRUE.equals(passwordPolicy.getForceUserToResetPasswordIfNotValid())) {
                try {
                    securitySettingService.validatePasswordByPolicy(password, passwordPolicy);
                } catch (Exception e) {
                    throw new UserPasswordNotValidException("The entered password violates our policies. If this is your real password, please reset it.");
                }
            }

            securityUser = authenticateByUsernameAndPassword(authentication, userPrincipal, username, password);
            if (mfaAuthService != null && mfaAuthService.isMfaEnabled(securityUser.getId())) {
                return new TwoFaAuthenticationToken(securityUser);
            }
        } else {
            String publicId = userPrincipal.getValue();
//            securityUser = authenticateByPublicId(userPrincipal, publicId);
        }

        return new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
    }

    private SecurityUser authenticateByUsernameAndPassword(Authentication authentication, UserPrincipal userPrincipal, String username, String password) {
        User user = userService.findUserByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        UserCredential userCredential = userService.findUserCredentialByUserId(user.getId());
        if (userCredential == null) {
            throw new UsernameNotFoundException("User credential not found");
        }

        securitySettingService.validateUserCredential(user.getTenantId(), userCredential, username, password);

        if (user.getAuthority() == null) {
            throw new InsufficientAuthenticationException("User has no authority assigned");
        }
        return new SecurityUser(user, userCredential.isEnabled(), userPrincipal);
    }

//    private SecurityUser authenticateByPublicId(UserPrincipal userPrincipal, String publicId) {
//        Merchant publicCustomer = merchantService.findMerchantById(Long.valueOf(publicId));
//        if (publicCustomer == null) {
//            throw new UsernameNotFoundException("Public entity not found: " + publicId);
//        }
//
//        if (!publicCustomer.isPublic()) {
//            throw new BadCredentialsException("Authentication Failed. Public Id is not valid.");
//        }
//
//        User user = new User();
//        user.setTenantId(publicCustomer.getTenantId());
//        user.setMerchantId(publicCustomer.getId());
//        user.setEmail(publicId);
//        user.setAuthority(Authority.MERCHANT_USER);
//        user.setName("Public");
//
//        return new SecurityUser(user, true, userPrincipal);
//    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
