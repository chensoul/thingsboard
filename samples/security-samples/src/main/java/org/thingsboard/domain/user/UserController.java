package org.thingsboard.domain.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.service.BaseController;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.notification.channel.mail.MailService;
import org.thingsboard.domain.setting.security.SecuritySettingService;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.domain.user.model.UserSetting;
import org.thingsboard.domain.user.model.UserSettingType;
import org.thingsboard.domain.user.service.UserSettingService;
import static org.thingsboard.domain.user.service.impl.UserServiceImpl.USER_CREDENTIALS_ENABLED;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.SecurityUtils;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.UserPrincipal;
import org.thingsboard.server.security.jwt.JwtTokenFactory;
import org.thingsboard.server.security.jwt.token.JwtPair;
import org.thingsboard.server.security.permission.Operation;
import org.thingsboard.server.security.permission.Resource;

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
	public static final String ACTIVATE_URL_PATTERN = "%s/api/noauth/activate?activateToken=%s";
	public static final String YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION = "You don't have permission to perform this operation!";

	@Value("${security.user_token_access_enabled}")
	private boolean userTokenAccessEnabled;

	private final MailService mailService;
	private final SecuritySettingService securitySettingService;
	private final UserSettingService userSettingService;
	private final JwtTokenFactory tokenFactory;

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
	@GetMapping(value = "/user/tokenAccessEnabled")
	public boolean isUserTokenAccessEnabled() {
		return userTokenAccessEnabled;
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@GetMapping(value = "/user/{userId}/token")
	public JwtPair getUserToken(@PathVariable(USER_ID) Long userId) {
		if (!userTokenAccessEnabled) {
			throw new ThingsboardException(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION,
				ThingsboardErrorCode.PERMISSION_DENIED);
		}
		User user = checkUserId(userId, Operation.READ);
		UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
		UserCredential credentials = userService.findUserCredentialByUserId(userId);
		SecurityUser securityUser = new SecurityUser(user, credentials.isEnabled(), principal);
		return tokenFactory.createTokenPair(securityUser);
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
	@PostMapping(value = "/user/sendActivationMail")
	@ResponseStatus(value = HttpStatus.OK)
	public void sendActivationEmail(@RequestParam(value = "email") String email,
									HttpServletRequest request) {
		User user = checkNotNull(userService.findUserByEmail(email));
		accessControlService.checkPermission(getCurrentUser(), Resource.USER, Operation.READ, user.getId(), user);

		UserCredential userCredential = userService.findUserCredentialByUserId(user.getId());
		if (!userCredential.isEnabled() && userCredential.getActivateToken() != null) {
			String baseUrl = securitySettingService.getBaseUrl(request);
			String activateUrl = String.format(ACTIVATE_URL_PATTERN, baseUrl, userCredential.getActivateToken());
			mailService.sendActivationEmail(activateUrl, email);
		} else {
			throw new ThingsboardException("User is already activated!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@GetMapping(value = "/user/{userId}/activationLink", produces = "text/plain")
	public String getActivationLink(@PathVariable(USER_ID) Long userId,
									HttpServletRequest request) {
		User user = checkUserId(userId, Operation.READ);
		UserCredential userCredential = userService.findUserCredentialByUserId(user.getId());
		if (!userCredential.isEnabled() && userCredential.getActivateToken() != null) {
			String baseUrl = securitySettingService.getBaseUrl(request);
			return String.format(ACTIVATE_URL_PATTERN, baseUrl, userCredential.getActivateToken());
		} else {
			throw new ThingsboardException("User is already activated!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
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
	public Page<User> getUsers(Pageable pageable, @RequestParam(required = false) String textSearch) {
		SecurityUser currentUser = getCurrentUser();
		if (Authority.SYS_ADMIN.equals(currentUser.getAuthority())) {
			return userService.findUsers(pageable, null, textSearch);
		} else if (Authority.TENANT_ADMIN.equals(currentUser.getAuthority())) {
			return userService.findTenantUsers(pageable, currentUser.getTenantId(), textSearch);
		} else {
			if (currentUser.getMerchantId() == null) {
				throw new ThingsboardException("merchantId is null", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
			}
			return userService.findMerchantUsers(pageable, Set.of(currentUser.getMerchantId()), textSearch);
		}
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@GetMapping(value = "/tenant/{tenantId}/users")
	public Page<User> getTenantAdmins(Pageable pageable, @PathVariable(TENANT_ID) String tenantId) {
		return userService.findTenantAdminsByTenantsIds(pageable, Set.of(tenantId));
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@GetMapping(value = "/merchant/{merchantId}/users")
	public Page<User> getMerchantUsers(Pageable pageable, @PathVariable(MERCHANT_ID) Long merchantId,
									   @RequestParam(required = false) String textSearch) {
		checkMerchantId(merchantId, Operation.READ);
		return userService.findMerchantUsers(pageable, Set.of(merchantId), textSearch);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@PostMapping(value = "/user/{userId}/userCredentialEnabled")
	public void setUserCredentialsEnabled(@PathVariable(USER_ID) Long userId,
										  @RequestParam(required = false, defaultValue = "true") boolean userCredentialEnabled) {
		User user = checkUserId(userId, Operation.WRITE);
		userService.setUserCredentialsEnabled(user.getId(), userCredentialEnabled);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@PostMapping(value = "/user/setting")
	public JsonNode saveUserSettings(@RequestBody JsonNode settings) {
		UserSetting userSetting = new UserSetting();
		userSetting.setType(UserSettingType.GENERAL);
		userSetting.setExtra(settings);
		userSetting.setUserId(SecurityUtils.getUserId());
		return userSettingService.saveUserSetting(userSetting).getExtra();
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@PutMapping(value = "/user/setting")
	public void putUserSettings(@RequestBody JsonNode settings) {
		userSettingService.updateUserSetting(SecurityUtils.getUserId(), UserSettingType.GENERAL, settings);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@GetMapping(value = "/user/setting")
	public JsonNode getUserSettings() {
		UserSetting userSetting = userSettingService.findUserSetting(SecurityUtils.getUserId(), UserSettingType.GENERAL);
		return userSetting == null ? JacksonUtil.newObjectNode() : userSetting.getExtra();
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@DeleteMapping(value = "/user/setting/{path}")
	public void deleteUserSettings(@PathVariable(PATH) String path) {
		userSettingService.deleteUserSetting(SecurityUtils.getUserId(), UserSettingType.GENERAL, Arrays.asList(path.split(",")));
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@PutMapping(value = "/user/setting/{type}")
	public void putUserSettings(@PathVariable("type") UserSettingType type, @RequestBody JsonNode settings) {
		userSettingService.updateUserSetting(SecurityUtils.getUserId(), type, settings);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@GetMapping(value = "/user/setting/{type}")
	public JsonNode getUserSettings(@PathVariable("type") UserSettingType type) {
		UserSetting userSetting = userSettingService.findUserSetting(SecurityUtils.getUserId(), type);
		return userSetting == null ? JacksonUtil.newObjectNode() : userSetting.getExtra();
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@DeleteMapping(value = "/user/setting/{type}/{path}")
	public void deleteUserSettings(@PathVariable(PATH) String path, @PathVariable("type") UserSettingType type) {
		userSettingService.deleteUserSetting(SecurityUtils.getUserId(), type, Arrays.asList(path.split(",")));
	}

}
