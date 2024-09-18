package com.chensoul.system.domain.setting.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SecuritySetting implements Serializable {
    private static final long serialVersionUID = -1307613974597312465L;
    private PasswordPolicy passwordPolicy = new PasswordPolicy();
    private Integer maxFailedLoginAttempts = 5;
    private String userLockoutNotificationEmail;

    @Data
    public static class PasswordPolicy implements Serializable {
        private Integer minimumLength = 6;
        private Integer maximumLength = 20;
        private Integer minimumUppercaseLetters;
        private Integer minimumLowercaseLetters;
        private Integer minimumDigits;
        private Integer minimumSpecialCharacters;
        private Boolean allowWhitespaces = true;
        private Boolean forceUserToResetPasswordIfNotValid = false;
        private Integer passwordExpirationPeriodDays;
        private Integer passwordReuseFrequencyDays;
    }
}
