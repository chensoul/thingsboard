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

import java.util.List;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderType;
import org.thingsboard.domain.user.TwoFaAuthController;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.jwt.token.JwtPair;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface TwoFaSettingAuthService {
	boolean isTwoFaEnabled(Long userId);

	void prepareVerificationCode(SecurityUser user, TwoFaProviderType providerType, boolean checkLimits) throws Exception;

	JwtPair checkVerificationCode(SecurityUser user, String verificationCode, TwoFaProviderType providerType, boolean checkLimits) throws ThingsboardException;

	List<TwoFaAuthController.TwoFaProviderInfo>  getAvailableTwoFaProviders();
}
