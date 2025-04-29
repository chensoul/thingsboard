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
package org.thingsboard.domain.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import static org.thingsboard.common.ControllerConstants.MERCHANT_ID;
import static org.thingsboard.common.ControllerConstants.PATH;
import static org.thingsboard.common.ControllerConstants.TENANT_ID;
import static org.thingsboard.common.ControllerConstants.USER_ID;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.BaseController;
import static org.thingsboard.domain.user.internal.UserServiceImpl.USER_CREDENTIALS_ENABLED;
import org.thingsboard.server.security.SecurityUtils;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.jwt.token.JwtPair;
import org.thingsboard.server.security.permission.Operation;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController extends BaseController {

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@GetMapping(value = "/user/{userId}")
	public User getUserById(@PathVariable(USER_ID) Long userId) {
		User user = checkUserId(userId, Operation.READ);

		if (user != null && user.getExtra().isObject()) {
			ObjectNode extra = (ObjectNode) user.getExtra();
			UserCredential userCredential = userService.findUserCredentialByUserId(user.getId());
			if (userCredential.isEnabled() && !extra.has(USER_CREDENTIALS_ENABLED)) {
				extra.put(USER_CREDENTIALS_ENABLED, true);
			}
		}
		return user;
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@GetMapping(value = "/user/{userId}/token")
	public JwtPair getUserToken(@PathVariable(USER_ID) Long userId) {
		User user = checkUserId(userId, Operation.READ);
		return userService.getUserToken(user);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@PostMapping(value = "/user")
	public User saveUser(@RequestBody User user,
						 @RequestParam(required = false, defaultValue = "true") boolean sendActivationMail) throws Exception {
		if (!Authority.SYS_ADMIN.equals(getCurrentUser().getAuthority())) {
			user.setTenantId(getCurrentUser().getTenantId());
		}
		User oldUser = checkUserId(user.getId(), Operation.WRITE);
		return doSaveAndLog(user, oldUser, EntityType.USER, (t) -> userService.saveUser(user, sendActivationMail));
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@DeleteMapping(value = "/user/{userId}")
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteUser(@PathVariable(USER_ID) Long userId) throws Exception {
		User old = checkUserId(userId, Operation.DELETE);
		doDeleteAndLog(old, EntityType.USER, (t) -> userService.deleteUser(old));
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN', 'MERCHANT_USER')")
	@GetMapping(value = "/users")
	public PageData<User> findUsers(PageLink pageLink) {
		return userService.findUsers(pageLink);
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@GetMapping(value = "/tenant/{tenantId}/users")
	public PageData<User> findUsersByTenantId(PageLink pageLink, @PathVariable(TENANT_ID) String tenantId) {
		return userService.findUsersByTenantId(tenantId, pageLink);
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@GetMapping(value = "/merchant/{merchantId}/users")
	public PageData<User> findUsersByMerchantId(PageLink pageLink, @PathVariable(MERCHANT_ID) Long merchantId,
											@RequestParam(required = false, defaultValue = "") String textSearch) {
		checkMerchantId(merchantId, Operation.READ);
		return userService.findUsersByMerchantIds(Set.of(merchantId), pageLink);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@PostMapping(value = "/user/{userId}/userCredential/enabled")
	public void setUserCredentialsEnabled(@PathVariable(USER_ID) Long userId,
										  @RequestParam(required = false, defaultValue = "true") boolean userCredentialEnabled) {
		User user = checkUserId(userId, Operation.WRITE);
		userService.setUserCredentialsEnabled(user.getId(), userCredentialEnabled);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@PutMapping(value = "/user/setting/{type}")
	public void putUserSetting(@PathVariable("type") UserSettingType type, @RequestBody JsonNode userSetting) {
		userSettingService.updateUserSetting(SecurityUtils.getUserId(), type, userSetting);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@GetMapping(value = "/user/setting/{type}")
	public UserSetting getUserSetting(@PathVariable("type") UserSettingType type) {
		return userSettingService.findUserSetting(SecurityUtils.getUserId(), type);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@DeleteMapping(value = "/user/setting/{type}/{path}")
	public void deleteUserSetting(@PathVariable(PATH) String path, @PathVariable("type") UserSettingType type) {
		userSettingService.deleteUserSetting(SecurityUtils.getUserId(), type, Arrays.asList(path.split(",")));
	}

}
