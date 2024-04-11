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
package org.thingsboard.domain.setting.mfa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.thingsboard.domain.setting.mfa.provider.TwoFaProviderConfig;
import org.thingsboard.domain.setting.mfa.provider.TwoFaProviderType;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwoFaSystemSetting {
	@Valid
	@NotNull
	private List<TwoFaProviderConfig> providers;

	@NotNull
	@Min(value = 5)
	private Integer minVerificationCodeSendPeriod;
	@Pattern(regexp = "[1-9]\\d*:[1-9]\\d*", message = "is invalid")
	private String verificationCodeCheckRateLimit;
	@Min(value = 0, message = "must be positive")
	private Integer maxVerificationFailuresBeforeUserLockout;
	@NotNull
	@Min(value = 60)
	private Integer totalAllowedTimeForVerification;

	public Optional<TwoFaProviderConfig> getProviderConfig(TwoFaProviderType providerType) {
		return Optional.ofNullable(providers)
			.flatMap(providersConfigs -> providersConfigs.stream()
				.filter(providerConfig -> providerConfig.getProviderType() == providerType)
				.findFirst());
	}

}
