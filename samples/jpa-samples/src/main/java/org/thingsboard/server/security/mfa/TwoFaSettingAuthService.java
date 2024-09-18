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
