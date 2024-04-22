/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.thingsboard.domain.tenant.model.Merchant;
import org.thingsboard.domain.tenant.service.MerchantService;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.domain.user.service.UserService;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.UserPrincipal;
import org.thingsboard.server.security.jwt.token.RawAccessJwtToken;
import org.thingsboard.server.security.jwt.token.RefreshAuthenticationToken;

@Component
@RequiredArgsConstructor
public class RefreshTokenAuthenticationProvider implements AuthenticationProvider {
	private final JwtTokenFactory tokenFactory;
	private final UserService userService;
	private final MerchantService merchantService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.notNull(authentication, "No authentication data provided");
		RawAccessJwtToken rawAccessToken = (RawAccessJwtToken) authentication.getCredentials();
		SecurityUser unsafeUser = tokenFactory.parseRefreshToken(rawAccessToken.getToken());
		UserPrincipal principal = unsafeUser.getUserPrincipal();

		SecurityUser securityUser;
		if (principal.getType() == UserPrincipal.Type.USER_NAME) {
			securityUser = authenticateByUserId(unsafeUser.getId());
		} else {
			securityUser = authenticateByPublicId(principal.getValue());
		}
		securityUser.setSessionId(unsafeUser.getSessionId());

		return new RefreshAuthenticationToken(securityUser);
	}

	private SecurityUser authenticateByUserId(Long userId) {
		User user = userService.findUserById(userId);
		if (user == null) {
			throw new UsernameNotFoundException("User not found by refresh token");
		}

		UserCredential userCredential = userService.findUserCredentialByUserId(user.getId());
		if (userCredential == null) {
			throw new UsernameNotFoundException("User credentials not found");
		}

		if (!userCredential.isEnabled()) {
			throw new DisabledException("User is not active");
		}

		if (user.getAuthority() == null) {
			throw new InsufficientAuthenticationException("User has no authority assigned");
		}

		UserPrincipal userPrincipal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
		SecurityUser securityUser = new SecurityUser(user, userCredential.isEnabled(), userPrincipal);
		return securityUser;
	}

	private SecurityUser authenticateByPublicId(String publicId) {
		Long customerId = Long.valueOf(publicId);

		Merchant publicCustomer = merchantService.findMerchantById(customerId);
		if (publicCustomer == null) {
			throw new UsernameNotFoundException("Public entity not found by refresh token");
		}

		if (!publicCustomer.isPublic()) {
			throw new BadCredentialsException("Refresh token is not valid");
		}

		User user = new User();
		user.setTenantId(publicCustomer.getTenantId());
		user.setMerchantId(publicCustomer.getId());
		user.setEmail(publicId);
		user.setAuthority(Authority.MERCHANT_USER);
		user.setName("Public");

		UserPrincipal userPrincipal = new UserPrincipal(UserPrincipal.Type.PUBLIC_ID, publicId);
		SecurityUser securityUser = new SecurityUser(user, true, userPrincipal);
		return securityUser;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (RefreshAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
