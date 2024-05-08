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
