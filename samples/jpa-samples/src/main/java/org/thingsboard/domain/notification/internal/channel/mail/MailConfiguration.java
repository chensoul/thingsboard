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

import lombok.Data;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
public class MailConfiguration {
	private long timeout;
	private String mailFrom;
	private String smtpHost;
	private int smtpPort;
	private String smtpProtocol;

	private String username;
	private String password;
	private boolean enableTls;
	private String tlsVersion;

	private boolean enableProxy;
	private String proxyHost;
	private int proxyPort;
	private String proxyUser;
	private String proxyPassword;

	private boolean showChangePassword;
	private boolean enableSsl;

	private boolean enableOauth2;
	private String clientId;
	private String clientSecret;
	private String refreshToken;
	private String providerId;
	private String providerTenantId;
	private boolean tokenGenerated;
	private String authUri;
	private String scope;
	private String redirectUri;
	private String tokenUri;
	private long refreshTokenExpires;

}
