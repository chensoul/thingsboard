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
package org.thingsboard.server.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.thingsboard.common.exception.ErrorResponseExceptionHandler;
import org.thingsboard.domain.limit.RateLimitProcessingFilter;
import org.thingsboard.server.security.jwt.JwtAuthenticationProvider;
import org.thingsboard.server.security.jwt.JwtTokenAuthenticationProcessingFilter;
import org.thingsboard.server.security.jwt.RefreshTokenAuthenticationProvider;
import org.thingsboard.server.security.jwt.RefreshTokenProcessingFilter;
import org.thingsboard.server.security.jwt.SkipPathRequestMatcher;
import org.thingsboard.server.security.jwt.extractor.TokenExtractor;
import org.thingsboard.server.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import org.thingsboard.server.security.oauth2.OAuth2Configuration;
import org.thingsboard.server.security.rest.RestAuthenticationProvider;
import org.thingsboard.server.security.rest.RestLoginProcessingFilter;
import org.thingsboard.server.security.rest.RestPublicLoginProcessingFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class SecurityConfiguration {


	public static final String DEVICE_API_ENTRY_POINT = "/api/v1/**";
	public static final String FORM_BASED_LOGIN_ENTRY_POINT = "/api/auth/login";
	public static final String PUBLIC_LOGIN_ENTRY_POINT = "/api/auth/login/public";
	public static final String TOKEN_REFRESH_ENTRY_POINT = "/api/auth/token";
	protected static final String[] NON_TOKEN_BASED_AUTH_ENTRY_POINTS = new String[]{"/index.html", "/api/noauth/**"};
	public static final String TOKEN_BASED_AUTH_ENTRY_POINT = "/api/**";
	public static final String WS_ENTRY_POINT = "/api/ws/**";
	public static final String MAIL_OAUTH2_PROCESSING_ENTRY_POINT = "/api/admin/mail/oauth2/code";
	public static final String DEVICE_CONNECTIVITY_CERTIFICATE_DOWNLOAD_ENTRY_POINT = "/api/device-connectivity/mqtts/certificate/download";

	@Autowired
	private ErrorResponseExceptionHandler restAccessDeniedHandler;

	@Autowired(required = false)
	@Qualifier("oauth2AuthenticationSuccessHandler")
	private AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

	@Autowired(required = false)
	@Qualifier("oauth2AuthenticationFailureHandler")
	private AuthenticationFailureHandler oauth2AuthenticationFailureHandler;

	@Autowired(required = false)
	private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Autowired(required = false)
	private OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver;

	@Autowired
	@Qualifier("defaultAuthenticationSuccessHandler")
	private AuthenticationSuccessHandler successHandler;

	@Autowired
	@Qualifier("defaultAuthenticationFailureHandler")
	private AuthenticationFailureHandler failureHandler;

	@Autowired
	private RestAuthenticationProvider restAuthenticationProvider;
	@Autowired
	private JwtAuthenticationProvider jwtAuthenticationProvider;
	@Autowired
	private RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider;

	@Autowired(required = false)
	OAuth2Configuration oauth2Configuration;

	@Autowired
	@Qualifier("jwtHeaderTokenExtractor")
	private TokenExtractor jwtHeaderTokenExtractor;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private RateLimitProcessingFilter rateLimitProcessingFilter;

	@Bean
	protected PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	protected RestLoginProcessingFilter buildRestLoginProcessingFilter() throws Exception {
		RestLoginProcessingFilter filter = new RestLoginProcessingFilter(FORM_BASED_LOGIN_ENTRY_POINT, successHandler, failureHandler);
		filter.setAuthenticationManager(this.authenticationManager);
		return filter;
	}

	@Bean
	protected RestPublicLoginProcessingFilter buildRestPublicLoginProcessingFilter() throws Exception {
		RestPublicLoginProcessingFilter filter = new RestPublicLoginProcessingFilter(PUBLIC_LOGIN_ENTRY_POINT, successHandler, failureHandler);
		filter.setAuthenticationManager(this.authenticationManager);
		return filter;
	}

	protected JwtTokenAuthenticationProcessingFilter buildJwtTokenAuthenticationProcessingFilter() throws Exception {
		List<String> pathsToSkip = new ArrayList<>(Arrays.asList(NON_TOKEN_BASED_AUTH_ENTRY_POINTS));
		pathsToSkip.addAll(Arrays.asList(WS_ENTRY_POINT, TOKEN_REFRESH_ENTRY_POINT, FORM_BASED_LOGIN_ENTRY_POINT,
			PUBLIC_LOGIN_ENTRY_POINT, DEVICE_API_ENTRY_POINT, MAIL_OAUTH2_PROCESSING_ENTRY_POINT,
			DEVICE_CONNECTIVITY_CERTIFICATE_DOWNLOAD_ENTRY_POINT));

		SkipPathRequestMatcher matcher = new SkipPathRequestMatcher(pathsToSkip, TOKEN_BASED_AUTH_ENTRY_POINT);
		JwtTokenAuthenticationProcessingFilter filter
			= new JwtTokenAuthenticationProcessingFilter(failureHandler, jwtHeaderTokenExtractor, matcher);
		filter.setAuthenticationManager(this.authenticationManager);
		return filter;
	}

	@Bean
	protected RefreshTokenProcessingFilter buildRefreshTokenProcessingFilter() throws Exception {
		RefreshTokenProcessingFilter filter = new RefreshTokenProcessingFilter(TOKEN_REFRESH_ENTRY_POINT, successHandler, failureHandler);
		filter.setAuthenticationManager(this.authenticationManager);
		return filter;
	}

	@Bean
	public AuthenticationManager authenticationManager(ObjectPostProcessor<Object> objectPostProcessor) throws Exception {
		DefaultAuthenticationEventPublisher eventPublisher = objectPostProcessor
			.postProcess(new DefaultAuthenticationEventPublisher());
		var auth = new AuthenticationManagerBuilder(objectPostProcessor);
		auth.authenticationEventPublisher(eventPublisher);
		auth.authenticationProvider(restAuthenticationProvider);
		auth.authenticationProvider(jwtAuthenticationProvider);
		auth.authenticationProvider(refreshTokenAuthenticationProvider);
		return auth.build();
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.headers(headers -> headers.cacheControl(config -> {
				})
				.frameOptions(config -> {
				}).disable())
			.cors(cors -> {
			})
			.csrf(AbstractHttpConfigurer::disable)
			.exceptionHandling(config -> {
			})
			.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeRequests(config -> config
				.requestMatchers(DEVICE_API_ENTRY_POINT).permitAll() // Device HTTP Transport API
				.requestMatchers(FORM_BASED_LOGIN_ENTRY_POINT).permitAll() // Login end-point
				.requestMatchers(PUBLIC_LOGIN_ENTRY_POINT).permitAll() // Public login end-point
				.requestMatchers(TOKEN_REFRESH_ENTRY_POINT).permitAll() // Token refresh end-point
				.requestMatchers(MAIL_OAUTH2_PROCESSING_ENTRY_POINT).permitAll() // Mail oauth2 code processing url
				.requestMatchers(DEVICE_CONNECTIVITY_CERTIFICATE_DOWNLOAD_ENTRY_POINT).permitAll() // Device connectivity certificate (public)
				.requestMatchers(NON_TOKEN_BASED_AUTH_ENTRY_POINTS).permitAll()// static resources, user activation and password reset end-points
				.requestMatchers(WS_ENTRY_POINT).permitAll() // Protected WebSocket API End-points
				.requestMatchers(TOKEN_BASED_AUTH_ENTRY_POINT).authenticated()) // Protected API End-points
			.exceptionHandling(config -> config.accessDeniedHandler(restAccessDeniedHandler))
			.addFilterBefore(buildRestLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(buildRestPublicLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(buildJwtTokenAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(buildRefreshTokenProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(rateLimitProcessingFilter, UsernamePasswordAuthenticationFilter.class)
		;

		if (oauth2Configuration != null) {
			http.oauth2Login(login -> login
				.authorizationEndpoint(config -> config
					.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
					.authorizationRequestResolver(oAuth2AuthorizationRequestResolver))
				.loginPage("/oauth2Login")
				.loginProcessingUrl(oauth2Configuration.getLoginProcessingUrl())
				.successHandler(oauth2AuthenticationSuccessHandler)
				.failureHandler(oauth2AuthenticationFailureHandler));
		}
		return http.build();
	}

//	@Bean
//	@ConditionalOnMissingBean(CorsFilter.class)
//	public CorsFilter corsFilter(@Autowired MvcCorsProperties mvcCorsProperties) {
//		if (mvcCorsProperties.getMappings().size() == 0) {
//			return new CorsFilter(new UrlBasedCorsConfigurationSource());
//		} else {
//			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//			source.setCorsConfigurations(mvcCorsProperties.getMappings());
//			return new CorsFilter(source);
//		}
//	}
}
