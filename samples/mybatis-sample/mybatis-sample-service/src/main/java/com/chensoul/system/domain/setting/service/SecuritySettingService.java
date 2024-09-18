package com.chensoul.system.domain.setting.service;


import com.chensoul.system.domain.setting.domain.MfaSetting;
import com.chensoul.system.domain.setting.domain.SecuritySetting;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.user.domain.UserCredential;

public interface SecuritySettingService {

    void validatePasswordByPolicy(String password, SecuritySetting.PasswordPolicy passwordPolicy);

    void validateUserCredential(String tenantId, UserCredential userCredential, String username, String password);

    void validateMfaVerification(SecurityUser securityUser, boolean verificationSuccess, MfaSetting twoFaSystemSettings);

    void validatePassword(String password, UserCredential userCredential);

}
