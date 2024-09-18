package com.chensoul.system.domain.notification.channel.mail;

import com.chensoul.system.domain.setting.domain.SystemSetting;
import org.springframework.mail.javamail.JavaMailSender;

public interface MailService {

  void updateMailConfiguration();

  void sendEmail(String email, String subject, String message);

  void sendTestMail(SystemSetting systemSetting, String email);

  void sendActivationEmail(String activationLink, String email);

  void sendAccountActivatedEmail(String loginLink, String email);

  void sendResetPasswordEmail(String passwordResetLink, String email);

  void sendResetPasswordEmailAsync(String passwordResetLink, String email);

  void sendPasswordWasResetEmail(String loginLink, String email);

  void sendAccountLockoutEmail(String lockoutEmail, String email, Integer maxFailedLoginAttempts);

  void sendTwoFaVerificationEmail(String email, String verificationCode, int expirationTimeSeconds);

  void send(String tenantId, Long customerId, Email email);

  void send(String tenantId, Long customerId, Email email, JavaMailSender javaMailSender, long timeout);

  void testConnection(String tenantId) throws Exception;

  boolean isConfigured(String tenantId);

}
