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
package org.thingsboard.server.security.oauth2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.oauth2.OAuth2MapperConfig;
import org.thingsboard.domain.oauth2.OAuth2Registration;
import org.thingsboard.domain.tenant.service.TenantProfileService;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.domain.user.service.UserService;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.UserPrincipal;

@Slf4j
@Component
public abstract class AbstractOAuth2ClientMapper {
	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	protected TenantProfileService tenantProfileService;

	private final Lock userCreationLock = new ReentrantLock();

	protected SecurityUser getOrCreateSecurityUserFromOAuth2User(OAuth2User oauth2User, OAuth2Registration registration) {
		OAuth2MapperConfig config = registration.getMapperConfig();
		UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, oauth2User.getEmail());

		User user = userService.findUserByEmail(oauth2User.getEmail());
		if (user == null && !config.isAllowUserCreation()) {
			throw new UsernameNotFoundException("User not found: " + oauth2User.getEmail());
		}

		if (user == null) {
			userCreationLock.lock();
			try {
				user = userService.findUserByEmail(oauth2User.getEmail());
				if (user == null) {
					user = new User();
					if (oauth2User.getMerchantId() == null && StringUtils.isEmpty(oauth2User.getMerchantName())) {
						user.setAuthority(Authority.TENANT_ADMIN);
					} else {
						user.setAuthority(Authority.MERCHANT_USER);
					}
					user.setTenantId(oauth2User.getTenantId());
					user.setMerchantId(oauth2User.getMerchantId());
					user.setEmail(oauth2User.getEmail());
					user.setName(oauth2User.getName());

					ObjectNode extra = JacksonUtil.newObjectNode();
					if (registration.getExtra() != null &&
						registration.getExtra().has("providerName")) {
						extra.put("authProviderName", registration.getExtra().get("providerName").asText());
					}
					user.setExtra(extra);

					user = userService.saveUser(user);
					if (config.isActivateUser()) {
						UserCredential userCredential = userService.findUserCredentialsByUserId(user.getId());
						userService.activateUserCredentials(userCredential.getActivateToken(), passwordEncoder.encode(""));
					}
				}
			} catch (Exception e) {
				log.error("Can't get or create security user from oauth2 user", e);
				throw new RuntimeException("Can't get or create security user from oauth2 user", e);
			} finally {
				userCreationLock.unlock();
			}
		}

		try {
			SecurityUser securityUser = new SecurityUser(user, true, principal);
			return (SecurityUser) new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()).getPrincipal();
		} catch (Exception e) {
			log.error("Can't get or create security user from oauth2 user", e);
			throw new RuntimeException("Can't get or create security user from oauth2 user", e);
		}
	}
}
