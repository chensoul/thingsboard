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

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.setting.SystemSetting;
import org.thingsboard.domain.setting.SystemSettingType;
import org.thingsboard.domain.setting.internal.persistence.SystemSettingDao;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

@Slf4j
public class MailSender extends JavaMailSenderImpl {
	private static final String MAIL_PROP = "mail.";
	private final SystemSettingDao systemSettingDao;
	private final Lock lock;

	private volatile String accessToken;

	@Getter
	private final Boolean oauth2Enabled;
	@Getter
	private volatile long tokenExpires;

	public MailSender(SystemSettingDao systemSettingDao, MailConfiguration mailConfiguration) {
		super();
		this.lock = new ReentrantLock();
		this.tokenExpires = 0L;
		this.systemSettingDao = systemSettingDao;
		this.oauth2Enabled = mailConfiguration.isEnableOauth2();

		setHost(mailConfiguration.getSmtpHost());
		setPort(mailConfiguration.getSmtpPort());
		setUsername(mailConfiguration.getUsername());
		if (mailConfiguration.getPassword() != null) {
			setPassword(mailConfiguration.getPassword());
		}
		setJavaMailProperties(createJavaMailProperties(mailConfiguration));
	}

	@Override
	protected void doSend(MimeMessage[] mimeMessages, @Nullable Object[] originalMessages) throws MailException {
		updateOauth2PasswordIfExpired();
		doSendSuper(mimeMessages, originalMessages);
	}

	public void doSendSuper(MimeMessage[] mimeMessages, Object[] originalMessages) {
		super.doSend(mimeMessages, originalMessages);
	}

	@Override
	public void testConnection() throws MessagingException {
		updateOauth2PasswordIfExpired();
		testConnectionSuper();
	}

	public void testConnectionSuper() throws MessagingException {
		super.testConnection();
	}

	public void updateOauth2PasswordIfExpired() {
		if (getOauth2Enabled() && (System.currentTimeMillis() > getTokenExpires())) {
			refreshAccessToken();
			setPassword(accessToken);
		}
	}

	private Properties createJavaMailProperties(MailConfiguration mailConfiguration) {
		Properties javaMailProperties = new Properties();
		String protocol = mailConfiguration.getSmtpProtocol();
		javaMailProperties.put("mail.transport.protocol", protocol);
		javaMailProperties.put(MAIL_PROP + protocol + ".host", mailConfiguration.getSmtpHost());
		javaMailProperties.put(MAIL_PROP + protocol + ".port", mailConfiguration.getSmtpPort());
		javaMailProperties.put(MAIL_PROP + protocol + ".timeout", mailConfiguration.getTimeout());
		javaMailProperties.put(MAIL_PROP + protocol + ".auth", String.valueOf(StringUtils.isNotEmpty(mailConfiguration.getUsername())));
		javaMailProperties.put(MAIL_PROP + protocol + ".starttls.enable", mailConfiguration.isEnableSsl());
		if (StringUtils.isNotBlank(mailConfiguration.getTlsVersion())) {
			javaMailProperties.put(MAIL_PROP + protocol + ".ssl.protocols", mailConfiguration.getTlsVersion());
		}

		if (mailConfiguration.isEnableProxy()) {
			javaMailProperties.put(MAIL_PROP + protocol + ".proxy.host", mailConfiguration.getProxyHost());
			javaMailProperties.put(MAIL_PROP + protocol + ".proxy.port", mailConfiguration.getProxyPort());
			String proxyUser = mailConfiguration.getProxyUser();
			if (StringUtils.isNoneEmpty(proxyUser)) {
				javaMailProperties.put(MAIL_PROP + protocol + ".proxy.user", proxyUser);
			}
			String proxyPassword = mailConfiguration.getProxyPassword();
			if (StringUtils.isNoneEmpty(proxyPassword)) {
				javaMailProperties.put(MAIL_PROP + protocol + ".proxy.password", proxyPassword);
			}
		}

		if (oauth2Enabled) {
			javaMailProperties.put(MAIL_PROP + protocol + ".auth.mechanisms", "XOAUTH2");
		}
		return javaMailProperties;
	}

	public void refreshAccessToken() {
		lock.lock();
		try {
			if (System.currentTimeMillis() > getTokenExpires()) {
				SystemSetting settings = systemSettingDao.findByType(SYS_TENANT_ID, SystemSettingType.EMAIL);
				MailConfiguration mailConfiguration = JacksonUtil.convertValue(settings.getExtra(), MailConfiguration.class);

				String clientId = mailConfiguration.getClientId();
				String clientSecret = mailConfiguration.getClientSecret();
				String refreshToken = mailConfiguration.getRefreshToken();
				String tokenUri = mailConfiguration.getTokenUri();
				String providerId = mailConfiguration.getProviderId();

				TokenResponse tokenResponse = new RefreshTokenRequest(new NetHttpTransport(), new GsonFactory(),
					new GenericUrl(tokenUri), refreshToken)
					.setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
					.execute();
				if (MailOauth2Provider.OFFICE_365.name().equals(providerId)) {
					mailConfiguration.setRefreshToken(tokenResponse.getRefreshToken());
					mailConfiguration.setRefreshTokenExpires(Instant.now().plus(Duration.ofDays(RefreshTokenExpCheckService.AZURE_DEFAULT_REFRESH_TOKEN_LIFETIME_IN_DAYS)).toEpochMilli());
					settings.setExtra(JacksonUtil.readTree(mailConfiguration));
					systemSettingDao.save(settings);
				}
				accessToken = tokenResponse.getAccessToken();
				tokenExpires = System.currentTimeMillis() + (tokenResponse.getExpiresInSeconds().intValue() * 1000);
			}
		} catch (Exception e) {
			log.error("Unable to retrieve access token: {}", e.getMessage());
			throw new RuntimeException("Error while retrieving access token: " + e.getMessage());
		} finally {
			lock.unlock();
		}
	}
}
