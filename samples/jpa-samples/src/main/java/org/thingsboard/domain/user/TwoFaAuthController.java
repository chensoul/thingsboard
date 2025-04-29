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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderType;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.jwt.token.JwtPair;
import org.thingsboard.server.security.mfa.TwoFaSettingAuthService;

@RestController
@RequestMapping("/api/auth/2fa")
@RequiredArgsConstructor
public class TwoFaAuthController {
	private final TwoFaSettingAuthService twoFaSettingAuthService;

	@PostMapping("/verification/send")
	@PreAuthorize("hasAuthority('PRE_VERIFICATION_TOKEN')")
	public void requestTwoFaVerificationCode(@RequestParam TwoFaProviderType providerType) throws Exception {
		twoFaSettingAuthService.prepareVerificationCode(getCurrentUser(), providerType, true);
	}

	@PostMapping("/verification/check")
	@PreAuthorize("hasAuthority('PRE_VERIFICATION_TOKEN')")
	public JwtPair checkTwoFaVerificationCode(@RequestParam TwoFaProviderType providerType, @RequestParam String verificationCode) throws Exception {
		return twoFaSettingAuthService.checkVerificationCode(getCurrentUser(), verificationCode, providerType, true);
	}

	@GetMapping("/providers")
	@PreAuthorize("hasAuthority('PRE_VERIFICATION_TOKEN')")
	public List<TwoFaProviderInfo> getAvailableTwoFaProviders() {
		return twoFaSettingAuthService.getAvailableTwoFaProviders();
	}

	@Data
	@AllArgsConstructor
	@Builder
	public static class TwoFaProviderInfo {
		private TwoFaProviderType type;
		private boolean useByDefault;
		private String contact;
		private Integer minVerificationCodeSendPeriod;
	}

}
