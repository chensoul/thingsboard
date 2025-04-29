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
package org.thingsboard.server.security.mfa;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import static org.apache.commons.lang3.StringUtils.repeat;
import org.springframework.stereotype.Service;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.setting.TwoFaSettingService;
import org.thingsboard.domain.setting.TwoFaSystemSetting;
import org.thingsboard.domain.setting.internal.mfa.config.EmailTwoFaConfig;
import org.thingsboard.domain.setting.internal.mfa.config.SmsTwoFaConfig;
import org.thingsboard.domain.setting.internal.mfa.config.TwoFaConfig;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderType;
import org.thingsboard.domain.setting.SecuritySettingService;
import org.thingsboard.domain.user.TwoFaAuthController;
import org.thingsboard.domain.user.UserService;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.jwt.JwtTokenFactory;
import org.thingsboard.server.security.jwt.token.JwtPair;
import org.thingsboard.server.security.rest.RestAuthenticationDetail;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class DefaultTwoFaSettingAuthService implements TwoFaSettingAuthService {
	public static final ThingsboardException USER_NOT_CONFIGURED_ERROR = new ThingsboardException("2FA is not configured for user", ThingsboardErrorCode.BAD_REQUEST_PARAMS);

	private final UserService userService;
	private final SecuritySettingService securitySettingService;
	private final TwoFaSettingService twoFaSettingService;
	private final JwtTokenFactory tokenFactory;
	private final HttpServletRequest request;

	@Override
	public boolean isTwoFaEnabled(Long userId) {
		return twoFaSettingService.getUserTwoFaSetting(userId).map(settings -> !settings.getConfigs().isEmpty()).orElse(false);
	}

	@Override
	public void prepareVerificationCode(SecurityUser user, TwoFaProviderType providerType, boolean checkLimits) throws Exception {
		TwoFaConfig accountConfig = twoFaSettingService.getUserTwoFaConfig(user.getId(), providerType).orElseThrow(() -> USER_NOT_CONFIGURED_ERROR);
		twoFaSettingService.prepareVerificationCode(user, accountConfig, checkLimits);
	}

	@Override
	public JwtPair checkVerificationCode(SecurityUser user, String verificationCode, TwoFaProviderType providerType, boolean checkLimits) {
		TwoFaConfig accountConfig = twoFaSettingService.getUserTwoFaConfig(user.getId(), providerType)
			.orElseThrow(() -> USER_NOT_CONFIGURED_ERROR);
		boolean verificationSuccess = twoFaSettingService.checkVerificationCode(user, verificationCode, accountConfig, checkLimits);

		if (verificationSuccess) {
			securitySettingService.logLoginAction(user, ActionType.LOGIN, null, new RestAuthenticationDetail(request), providerType.name());
			user = new SecurityUser(userService.findUserById(user.getId()), true, user.getUserPrincipal());
			return tokenFactory.createTokenPair(user);
		} else {
			ThingsboardException error = new ThingsboardException("Verification code is incorrect", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
			securitySettingService.logLoginAction(user, ActionType.LOGIN, error, new RestAuthenticationDetail(request), providerType.name());
			throw error;
		}
	}

	@Override
	public List<TwoFaAuthController.TwoFaProviderInfo> getAvailableTwoFaProviders() {
		SecurityUser user = getCurrentUser();
		Optional<TwoFaSystemSetting> twoFaSystemSetting = twoFaSettingService.getTwoFaSystemSetting(true);
		return twoFaSettingService.getUserTwoFaSetting(user.getId())
			.map(settings -> settings.getConfigs().values()).orElse(Collections.emptyList())
			.stream().map(config -> {
				String contact = null;
				switch (config.getProviderType()) {
					case SMS:
						String phoneNumber = ((SmsTwoFaConfig) config).getPhoneNumber();
						contact = obfuscate(phoneNumber, 2, '*', phoneNumber.indexOf('+') + 1, phoneNumber.length());
						break;
					case EMAIL:
						String email = ((EmailTwoFaConfig) config).getEmail();
						contact = obfuscate(email, 2, '*', 0, email.indexOf('@'));
						break;
				}
				return TwoFaAuthController.TwoFaProviderInfo.builder()
					.type(config.getProviderType())
					.useByDefault(config.isUseByDefault())
					.contact(contact)
					.minVerificationCodeSendPeriod(twoFaSystemSetting.get().getMinVerificationCodeSendPeriod())
					.build();
			})
			.collect(Collectors.toList());
	}

	private static String obfuscate(String input, int seenMargin, char obfuscationChar,
									int startIndexInclusive, int endIndexExclusive) {
		String part = input.substring(startIndexInclusive, endIndexExclusive);
		String obfuscatedPart;
		if (part.length() <= seenMargin * 2) {
			obfuscatedPart = repeat(obfuscationChar, part.length());
		} else {
			obfuscatedPart = part.substring(0, seenMargin)
							 + repeat(obfuscationChar, part.length() - seenMargin * 2)
							 + part.substring(part.length() - seenMargin);
		}
		return input.substring(0, startIndexInclusive) + obfuscatedPart + input.substring(endIndexExclusive);
	}
}
