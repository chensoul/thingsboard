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
package org.thingsboard.domain.setting.mfa.provider;


import org.thingsboard.domain.user.model.User;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.domain.setting.mfa.config.TwoFaConfig;

public interface TwoFaProvider<C extends TwoFaProviderConfig, A extends TwoFaConfig> {

	A generateTwoFaConfig(User user, C providerConfig);

	default void prepareVerificationCode(SecurityUser user, C providerConfig, A accountConfig) {
	}

	boolean checkVerificationCode(SecurityUser user, String code, C providerConfig, A accountConfig);

	default void check(String tenantId) {
	}

	TwoFaProviderType getType();

}
