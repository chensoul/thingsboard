package com.chensoul.system.infrastructure.security.mfa.provider.impl;

import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.notification.channel.mail.MailService;
import com.chensoul.system.infrastructure.security.mfa.config.EmailMfaConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.EmailMfaProviderConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.user.domain.User;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class EmailMfaProvider extends OtpBasedMfaProvider<EmailMfaProviderConfig, EmailMfaConfig> {
    private final MailService mailService;

    protected EmailMfaProvider(CacheManager cacheManager, MailService mailService) {
        super(cacheManager);
        this.mailService = mailService;
    }

    @Override
    public EmailMfaConfig generateTwoFaConfig(User user, EmailMfaProviderConfig providerConfig) {
        EmailMfaConfig config = new EmailMfaConfig();
        config.setEmail(user.getEmail());
        return config;
    }

    @Override
    public void check(String tenantId) {
        try {
            mailService.testConnection(tenantId);
        } catch (Exception e) {
            throw new BusinessException("Mail service is not set up");
        }
    }

    @Override
    protected void sendVerificationCode(SecurityUser user, String verificationCode, EmailMfaProviderConfig providerConfig, EmailMfaConfig accountConfig) {
        mailService.sendTwoFaVerificationEmail(accountConfig.getEmail(), verificationCode, providerConfig.getVerificationCodeLifetime());
    }

    @Override
    public MfaProviderType getType() {
        return MfaProviderType.EMAIL;
    }

}
