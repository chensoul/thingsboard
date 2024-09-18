package com.chensoul.system.domain.setting.controller;

import com.chensoul.system.domain.setting.domain.MfaConfigRequest;
import com.chensoul.system.domain.setting.domain.MfaSetting;
import com.chensoul.system.domain.setting.service.MfaSettingService;
import com.chensoul.system.infrastructure.security.mfa.config.MfaConfig;
import com.chensoul.system.infrastructure.security.mfa.config.UserMfaConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.chensoul.system.infrastructure.security.util.SecurityUtils;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getCurrentUser;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getUserId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
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

@Validated
@RestController
@RequestMapping("/api/systems/setting/mfa")
@RequiredArgsConstructor
public class MfaSettingController {
    private final MfaSettingService mfaSettingService;

    @GetMapping("/user")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public UserMfaConfig getUserMfaSetting() {
        return mfaSettingService.getUserMfaConfig(getUserId()).orElse(null);
    }

    @PostMapping("/user/generate")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public MfaConfig generateUserTwoFaConfig(@RequestParam MfaProviderType providerType) throws Exception {
        return mfaSettingService.generateUserMfaConfig(getCurrentUser(), providerType);
    }

    @PostMapping("/user/submit")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public void prepareVerificationCode(@Valid @RequestBody MfaConfig mfaConfig) throws Exception {
        mfaSettingService.prepareVerificationCode(getCurrentUser(), mfaConfig, false);
    }

    @PostMapping("/user")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public UserMfaConfig verifyAndSaveUserTwoFaConfig(@Valid @RequestBody MfaConfig accountConfig,
                                                      @RequestParam(required = false) String verificationCode) throws Exception {
        return mfaSettingService.verifyAndSaveUserMfaConfig(getCurrentUser(), accountConfig, verificationCode);
    }

    @PutMapping("/user")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public UserMfaConfig updateUserTwoFaConfig(@RequestParam MfaProviderType providerType,
                                               @RequestBody MfaConfigRequest updateRequest) {
        return mfaSettingService.updateUserMfaConfig(getUserId(), providerType, updateRequest);
    }

    @DeleteMapping("/user")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public UserMfaConfig deleteUserTwoFaConfig(@RequestParam MfaProviderType providerType) {
        return mfaSettingService.deleteUserMfaConfig(getUserId(), providerType);
    }

    @GetMapping("/provider")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public List<MfaProviderType> getAvailableTwoFaProviders() {
        return mfaSettingService.getSystemMfaSetting(true)
            .map(MfaSetting::getProviders).orElse(Collections.emptyList()).stream()
            .map(MfaProviderConfig::getProviderType)
            .collect(Collectors.toList());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    public MfaSetting getTwoFaSystemSetting() {
        return mfaSettingService.getSystemMfaSetting(false).orElse(null);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    public MfaSetting saveTwoFaSystemSetting(@RequestBody @Valid MfaSetting twoFaSystemSettings) {
        return mfaSettingService.saveSystemMfaSetting(SecurityUtils.getTenantId(), twoFaSystemSettings);
    }
}
