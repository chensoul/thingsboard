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
package org.thingsboard.server.mail;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.mail.javamail.JavaMailSender;
import org.thingsboard.common.exception.ThingsboardException;

public interface MailService {

	void updateMailConfiguration();

	void sendEmail(String email, String subject, String message) throws ThingsboardException;

	void sendTestMail(JsonNode config, String email) throws ThingsboardException;

	void sendActivationEmail(String activationLink, String email) throws ThingsboardException;

	void sendAccountActivatedEmail(String loginLink, String email) throws ThingsboardException;

	void sendResetPasswordEmail(String passwordResetLink, String email) throws ThingsboardException;

	void sendResetPasswordEmailAsync(String passwordResetLink, String email);

	void sendPasswordWasResetEmail(String loginLink, String email) throws ThingsboardException;

	void sendAccountLockoutEmail(String lockoutEmail, String email, Integer maxFailedLoginAttempts) throws ThingsboardException;

	void sendTwoFaVerificationEmail(String email, String verificationCode, int expirationTimeSeconds) throws ThingsboardException;

	void send(String tenantId, Long customerId, Email email) throws ThingsboardException;

	void send(String tenantId, Long customerId, Email email, JavaMailSender javaMailSender, long timeout) throws ThingsboardException;

	void testConnection(String tenantId) throws Exception;

	boolean isConfigured(String tenantId);

}
