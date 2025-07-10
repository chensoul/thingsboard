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
package org.thingsboard.server.security.oauth2.mapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.oauth2.OAuth2MapperConfig;
import org.thingsboard.domain.oauth2.OAuth2Registration;
import org.thingsboard.domain.merchant.Merchant;
import org.thingsboard.domain.tenant.Tenant;
import org.thingsboard.domain.merchant.MerchantService;
import org.thingsboard.domain.tenant.TenantProfileService;
import org.thingsboard.domain.tenant.TenantService;
import org.thingsboard.domain.user.Authority;
import org.thingsboard.domain.user.User;
import org.thingsboard.domain.user.UserCredential;
import org.thingsboard.domain.user.UserService;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.UserPrincipal;
import org.thingsboard.server.security.oauth2.OAuth2User;

@Slf4j
@Component
public abstract class AbstractOAuth2ClientMapper implements OAuth2ClientMapper {
	@Autowired
	private UserService userService;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private MerchantService merchantService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	protected TenantProfileService tenantProfileService;

	private final Lock userCreationLock = new ReentrantLock();

	@Override
	public SecurityUser getOrCreateUserByClientPrincipal(HttpServletRequest request, OAuth2AuthenticationToken token, String providerAccessToken, OAuth2Registration registration) {
		OAuth2User oauth2User = getOAuth2User(request, token, providerAccessToken, registration);

		return getOrCreateSecurityUserFromOAuth2User(oauth2User, registration);
	}

	protected abstract OAuth2User getOAuth2User(HttpServletRequest request, OAuth2AuthenticationToken token, String providerAccessToken, OAuth2Registration registration);

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

					String tenantId = oauth2User.getTenantId() != null ? oauth2User.getTenantId() : getTenantId(oauth2User.getTenantName());
					user.setTenantId(tenantId);

					Long merchantId = oauth2User.getMerchantId() != null ?
						oauth2User.getMerchantId() : getMerchantId(user.getTenantId(), oauth2User.getMerchantName());
					user.setMerchantId(merchantId);
					user.setEmail(oauth2User.getEmail());
					user.setName(oauth2User.getName());

					ObjectNode extra = JacksonUtil.newObjectNode();
					extra.put("authProviderId", registration.getProviderId());
					user.setExtra(extra);

					user = userService.saveUser(user);
					if (config.isActivateUser()) {
						UserCredential userCredential = userService.findUserCredentialByUserId(user.getId());
						userService.activateUserCredential(userCredential.getActivateToken(), passwordEncoder.encode(""));
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

	private String getTenantId(String tenantName) throws Exception {
		Tenant tenant = tenantService.findTenantByName(tenantName);

		if (tenant == null) {
			tenant = new Tenant();
			tenant.setName(tenantName);
			tenant = tenantService.saveTenant(tenant);
		}
		return tenant.getTenantId();
	}

	private Long getMerchantId(String tenantId, String merchantName) {
		if (StringUtils.isEmpty(merchantName)) {
			return null;
		}
		Optional<Merchant> merchantOptional = merchantService.findMerchantByTenantIdAndName(tenantId, merchantName);
		if (merchantOptional.isPresent()) {
			return merchantOptional.get().getId();
		} else {
			Merchant customer = new Merchant();
			customer.setTenantId(tenantId);
			customer.setName(merchantName);
			return merchantService.saveMerchant(customer).getId();
		}
	}
}
