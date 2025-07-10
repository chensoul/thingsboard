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
package org.thingsboard.domain.notification.internal.channel.mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import jakarta.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.setting.SystemSetting;
import org.thingsboard.domain.setting.SystemSettingType;
import org.thingsboard.domain.setting.internal.persistence.SystemSettingDao;
import org.thingsboard.domain.usage.ApiFeature;
import org.thingsboard.domain.usage.ApiUsageRecordState;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultMailService implements MailService {
	public static final String TARGET_EMAIL = "targetEmail";
	public static final String UTF_8 = "UTF-8";
	private static final long DEFAULT_TIMEOUT = 10_000;

	private MailSender mailSender;
	private MailConfiguration mailConfiguration;
	private String mailFrom;
	private long timeout;

	private final MessageSource messages;
	private final Configuration freemarkerConfig;
	private final SystemSettingDao systemSettingDao;
	private final MailExecutorService mailExecutorService;
	private final PasswordResetExecutorService passwordResetExecutorService;

	@PostConstruct
	private void init() {
		updateMailConfiguration();
	}

	@Override
	public void updateMailConfiguration() {
		SystemSetting systemSetting = systemSettingDao.findByType(SYS_TENANT_ID, SystemSettingType.EMAIL);
		if (systemSetting != null && systemSetting.getExtra() != null) {
			JsonNode jsonConfig = systemSetting.getExtra();
			mailConfiguration = JacksonUtil.convertValue(jsonConfig, MailConfiguration.class);
			mailFrom = mailConfiguration.getMailFrom();
			timeout = mailConfiguration.getTimeout() == 0 ? DEFAULT_TIMEOUT : mailConfiguration.getTimeout();
			mailSender = new MailSender(systemSettingDao, mailConfiguration);
		} else {
			throw new DataValidationException("Failed to update mail configuration. Settings not found!");
		}
	}

	@Override
	public void sendEmail(String email, String subject, String message) {
		sendMail(mailSender, mailFrom, email, subject, message, timeout);
	}

	@Override
	public void sendTestMail(SystemSetting systemSetting, String email) {
		if (systemSetting.getExtra().has("enableOauth2") && systemSetting.getExtra().get("enableOauth2").asBoolean()) {
			SystemSetting mailSettings = systemSettingDao.findByType(SYS_TENANT_ID, SystemSettingType.EMAIL);
			JsonNode refreshToken = mailSettings.getExtra().get("refreshToken");
			if (refreshToken == null) {
				throw new ThingsboardException("Refresh token was not generated. Please, generate refresh token.", ThingsboardErrorCode.GENERAL);
			}
			ObjectNode settings = (ObjectNode) systemSetting.getExtra();
			settings.put("refreshToken", refreshToken.asText());
		} else {
			if (!systemSetting.getExtra().has("password")) {
				SystemSetting mailSettings = systemSettingDao.findByType(SYS_TENANT_ID, SystemSettingType.EMAIL);
				((ObjectNode) systemSetting.getExtra()).put("password", mailSettings.getExtra().get("password").asText());
			}
		}
		MailConfiguration configuration = JacksonUtil.convertValue(systemSetting.getExtra(), MailConfiguration.class);

		MailSender testMailSender = new MailSender(systemSettingDao, mailConfiguration);
		String mailFrom = configuration.getMailFrom();
		String subject = messages.getMessage("test.message.subject", null, Locale.US);
		Map<String, Object> model = new HashMap<>();
		model.put(TARGET_EMAIL, email);

		String message = mergeTemplateIntoString("test.ftl", model);

		sendMail(testMailSender, mailFrom, email, subject, message, timeout);
	}

	@Override
	public void sendActivationEmail(String activationLink, String email) {
		String subject = messages.getMessage("activation.subject", null, Locale.US);

		Map<String, Object> model = new HashMap<>();
		model.put("activationLink", activationLink);
		model.put(TARGET_EMAIL, email);

		String message = mergeTemplateIntoString("activation.ftl", model);

		sendMail(mailSender, mailFrom, email, subject, message, timeout);
	}

	@Override
	public void sendAccountActivatedEmail(String loginLink, String email) {
		String subject = messages.getMessage("account.activated.subject", null, Locale.US);

		Map<String, Object> model = new HashMap<>();
		model.put("loginLink", loginLink);
		model.put(TARGET_EMAIL, email);

		String message = mergeTemplateIntoString("account.activated.ftl", model);

		sendMail(mailSender, mailFrom, email, subject, message, timeout);
	}

	@Override
	public void sendResetPasswordEmail(String passwordResetLink, String email) {
		String subject = messages.getMessage("reset.password.subject", null, Locale.US);

		Map<String, Object> model = new HashMap<>();
		model.put("passwordResetLink", passwordResetLink);
		model.put(TARGET_EMAIL, email);

		String message = mergeTemplateIntoString("reset.password.ftl", model);

		sendMail(mailSender, mailFrom, email, subject, message, timeout);
	}

	@Override
	public void sendResetPasswordEmailAsync(String passwordResetLink, String email) {
		passwordResetExecutorService.execute(() -> {
			try {
				this.sendResetPasswordEmail(passwordResetLink, email);
			} catch (Exception e) {
				log.error("Error occurred: {} ", e.getMessage());
			}
		});
	}

	@Override
	public void sendPasswordWasResetEmail(String loginLink, String email) {
		String subject = messages.getMessage("password.was.reset.subject", null, Locale.US);

		Map<String, Object> model = new HashMap<>();
		model.put("loginLink", loginLink);
		model.put(TARGET_EMAIL, email);

		String message = mergeTemplateIntoString("password.was.reset.ftl", model);

		sendMail(mailSender, mailFrom, email, subject, message, timeout);
	}

	@Override
	public void send(String tenantId, Long customerId, Email email) {
		sendMail(tenantId, customerId, email, this.mailSender, timeout);
	}

	@Override
	public void send(String tenantId, Long customerId, Email email, JavaMailSender javaMailSender, long timeout) {
		sendMail(tenantId, customerId, email, javaMailSender, timeout);
	}

	private void sendMail(String tenantId, Long customerId, Email email, JavaMailSender javaMailSender, long timeout) {
		try {
			MimeMessage mailMsg = javaMailSender.createMimeMessage();
			boolean multipart = (email.getImages() != null && !email.getImages().isEmpty());
			MimeMessageHelper helper = new MimeMessageHelper(mailMsg, multipart, "UTF-8");
			helper.setFrom(StringUtils.isBlank(email.getFrom()) ? mailFrom : email.getFrom());
			helper.setTo(email.getTo().split("\\s*,\\s*"));
			if (!StringUtils.isBlank(email.getCc())) {
				helper.setCc(email.getCc().split("\\s*,\\s*"));
			}
			if (!StringUtils.isBlank(email.getBcc())) {
				helper.setBcc(email.getBcc().split("\\s*,\\s*"));
			}
			helper.setSubject(email.getSubject());
			helper.setText(email.getBody(), email.isHtml());

			if (multipart) {
				for (String imgId : email.getImages().keySet()) {
					String imgValue = email.getImages().get(imgId);
					String value = imgValue.replaceFirst("^data:image/[^;]*;base64,?", "");
					byte[] bytes = DatatypeConverter.parseBase64Binary(value);
					String contentType = helper.getFileTypeMap().getContentType(imgId);
					InputStreamSource iss = () -> new ByteArrayInputStream(bytes);
					helper.addInline(imgId, iss, contentType);
				}
			}
			sendMailWithTimeout(javaMailSender, helper.getMimeMessage(), timeout);
//				apiUsageClient.report(tenantId, customerId, ApiUsageRecordKey.EMAIL_EXEC_COUNT, 1);
		} catch (Exception e) {
			throw handleException(e);
		}

	}

	@Override
	public void sendAccountLockoutEmail(String lockoutEmail, String email, Integer maxFailedLoginAttempts) {
		String subject = messages.getMessage("account.lockout.subject", null, Locale.US);

		Map<String, Object> model = new HashMap<>();
		model.put("lockoutAccount", lockoutEmail);
		model.put("maxFailedLoginAttempts", maxFailedLoginAttempts);
		model.put(TARGET_EMAIL, email);

		String message = mergeTemplateIntoString("account.lockout.ftl", model);

		sendMail(mailSender, mailFrom, email, subject, message, timeout);
	}

	@Override
	public void sendTwoFaVerificationEmail(String email, String verificationCode, int expirationTimeSeconds) {
		String subject = messages.getMessage("2fa.verification.code.subject", null, Locale.US);
		String message = mergeTemplateIntoString("2fa.verification.code.ftl", Map.of(
			TARGET_EMAIL, email,
			"code", verificationCode,
			"expirationTimeSeconds", expirationTimeSeconds
		));

		sendMail(mailSender, mailFrom, email, subject, message, timeout);
	}

	@Override
	public void testConnection(String tenantId) throws Exception {
		mailSender.testConnection();
	}

	@Override
	public boolean isConfigured(String tenantId) {
		return mailSender != null;
	}

	private String toEnabledValueLabel(ApiFeature apiFeature) {
		switch (apiFeature) {
			case DB:
				return "save";
			case TRANSPORT:
				return "receive";
			case JS:
				return "invoke";
			case RE:
				return "process";
			case EMAIL:
			case SMS:
				return "send";
			case ALARM:
				return "create";
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	private String toDisabledValueLabel(ApiFeature apiFeature) {
		switch (apiFeature) {
			case DB:
				return "saved";
			case TRANSPORT:
				return "received";
			case JS:
				return "invoked";
			case RE:
				return "processed";
			case EMAIL:
			case SMS:
				return "sent";
			case ALARM:
				return "created";
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	private String toWarningValueLabel(ApiUsageRecordState recordState) {
		String valueInM = recordState.getValueAsString();
		String thresholdInM = recordState.getThresholdAsString();
		switch (recordState.getKey()) {
			case STORAGE_DP_COUNT:
			case TRANSPORT_DP_COUNT:
				return valueInM + " out of " + thresholdInM + " allowed data points";
			case TRANSPORT_MSG_COUNT:
				return valueInM + " out of " + thresholdInM + " allowed messages";
			case JS_EXEC_COUNT:
				return valueInM + " out of " + thresholdInM + " allowed JavaScript functions";
			case TBEL_EXEC_COUNT:
				return valueInM + " out of " + thresholdInM + " allowed Tbel functions";
			case RE_EXEC_COUNT:
				return valueInM + " out of " + thresholdInM + " allowed Rule Engine messages";
			case EMAIL_EXEC_COUNT:
				return valueInM + " out of " + thresholdInM + " allowed Email messages";
			case SMS_EXEC_COUNT:
				return valueInM + " out of " + thresholdInM + " allowed SMS messages";
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	private String toDisabledValueLabel(ApiUsageRecordState recordState) {
		switch (recordState.getKey()) {
			case STORAGE_DP_COUNT:
			case TRANSPORT_DP_COUNT:
				return recordState.getValueAsString() + " data points";
			case TRANSPORT_MSG_COUNT:
				return recordState.getValueAsString() + " messages";
			case JS_EXEC_COUNT:
				return "JavaScript functions " + recordState.getValueAsString() + " times";
			case TBEL_EXEC_COUNT:
				return "TBEL functions " + recordState.getValueAsString() + " times";
			case RE_EXEC_COUNT:
				return recordState.getValueAsString() + " Rule Engine messages";
			case EMAIL_EXEC_COUNT:
				return recordState.getValueAsString() + " Email messages";
			case SMS_EXEC_COUNT:
				return recordState.getValueAsString() + " SMS messages";
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	private void sendMail(JavaMailSenderImpl mailSender, String mailFrom, String email,
						  String subject, String message, long timeout) {
		try {
			MimeMessage mimeMsg = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, UTF_8);
			helper.setFrom(mailFrom);
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(message, true);

			sendMailWithTimeout(mailSender, helper.getMimeMessage(), timeout);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	private void sendMailWithTimeout(JavaMailSender mailSender, MimeMessage msg, long timeout) {
		try {
			mailExecutorService.submit(() -> mailSender.send(msg)).get(timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			log.debug("Error during mail submission", e);
			throw new RuntimeException("Timeout!");
		} catch (Exception e) {
			throw new RuntimeException(ExceptionUtils.getRootCause(e));
		}
	}

	private String mergeTemplateIntoString(String templateLocation,
										   Map<String, Object> model) {
		try {
			Template template = freemarkerConfig.getTemplate(templateLocation);
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	protected ThingsboardException handleException(Exception exception) {
		String message;
		if (exception instanceof NestedRuntimeException) {
			message = ((NestedRuntimeException) exception).getMostSpecificCause().getMessage();
		} else {
			message = exception.getMessage();
		}
		log.warn("Unable to send mail: {}", message);
		return new ThingsboardException(String.format("Unable to send mail: %s", message),
			ThingsboardErrorCode.GENERAL);
	}

}
