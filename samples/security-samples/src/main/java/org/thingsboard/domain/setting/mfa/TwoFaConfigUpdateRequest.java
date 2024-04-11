package org.thingsboard.domain.setting.mfa;

import lombok.Data;

@Data
public class TwoFaConfigUpdateRequest {
	private boolean useByDefault;
}
