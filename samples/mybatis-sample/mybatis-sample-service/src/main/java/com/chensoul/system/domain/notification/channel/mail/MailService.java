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
