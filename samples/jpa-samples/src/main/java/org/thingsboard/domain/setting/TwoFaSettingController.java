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
package org.thingsboard.domain.setting;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.domain.setting.internal.mfa.config.TwoFaConfig;
import org.thingsboard.domain.setting.internal.mfa.config.UserTwoFaSetting;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderConfig;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderType;
import org.thingsboard.server.security.SecurityUtils;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import static org.thingsboard.server.security.SecurityUtils.getUserId;

@Validated
@RestController
@RequestMapping("/api/system/2faSetting")
@RequiredArgsConstructor
public class TwoFaSettingController {
	private final TwoFaSettingService twoFaSettingService;

	@GetMapping("/user")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public UserTwoFaSetting getUserTwoFaSetting() {
		return twoFaSettingService.getUserTwoFaSetting(getUserId()).orElse(null);
	}

	@PostMapping("/user/generate")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public TwoFaConfig generateUserTwoFaConfig(@RequestParam TwoFaProviderType providerType) throws Exception {
		return twoFaSettingService.generateUserTwoFaConfig(getCurrentUser(), providerType);
	}

	@PostMapping("/user/submit")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public void prepareVerificationCode(@Valid @RequestBody TwoFaConfig twoFaConfig) throws Exception {
		twoFaSettingService.prepareVerificationCode(getCurrentUser(), twoFaConfig, false);
	}

	@PostMapping("/user")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public UserTwoFaSetting verifyAndSaveUserTwoFaConfig(@Valid @RequestBody TwoFaConfig accountConfig,
														 @RequestParam(required = false) String verificationCode) throws Exception {
		return twoFaSettingService.verifyAndSaveUserTwoFaConfig(getCurrentUser(), accountConfig, verificationCode);
	}

	@PutMapping("/user")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public UserTwoFaSetting updateUserTwoFaConfig(@RequestParam TwoFaProviderType providerType,
												  @RequestBody TwoFaConfigUpdateRequest updateRequest) {
		return twoFaSettingService.updateUserTwoFaConfig(getUserId(), providerType, updateRequest);
	}

	@DeleteMapping("/user")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public UserTwoFaSetting deleteUserTwoFaConfig(@RequestParam TwoFaProviderType providerType) {
		return twoFaSettingService.deleteUserTwoFaConfig(getUserId(), providerType);
	}

	@GetMapping("/provider")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public List<TwoFaProviderType> getAvailableTwoFaProviders() {
		return twoFaSettingService.getTwoFaSystemSetting(true)
			.map(TwoFaSystemSetting::getProviders).orElse(Collections.emptyList()).stream()
			.map(TwoFaProviderConfig::getProviderType)
			.collect(Collectors.toList());
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	public TwoFaSystemSetting getTwoFaSystemSetting() {
		return twoFaSettingService.getTwoFaSystemSetting(false).orElse(null);
	}

	@PostMapping
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	public TwoFaSystemSetting saveTwoFaSystemSetting(@RequestBody @Valid TwoFaSystemSetting twoFaSystemSettings) {
		return twoFaSettingService.saveTwoFaSystemSetting(SecurityUtils.getTenantId(), twoFaSystemSettings);
	}
}
