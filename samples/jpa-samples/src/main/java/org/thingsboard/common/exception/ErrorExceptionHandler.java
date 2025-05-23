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
package org.thingsboard.common.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.security.jwt.JwtExpiredTokenException;
import org.thingsboard.server.security.rest.exception.AuthMethodNotSupportedException;
import org.thingsboard.server.security.rest.exception.CredentialsExpiredResponse;
import org.thingsboard.server.security.rest.exception.UserPasswordExpiredException;
import org.thingsboard.server.security.rest.exception.UserPasswordNotValidException;

@Slf4j
@Controller
@RequiredArgsConstructor
@RestControllerAdvice
public class ErrorExceptionHandler extends ResponseEntityExceptionHandler implements AccessDeniedHandler, ErrorController {
	private static final Map<HttpStatus, ThingsboardErrorCode> statusToErrorCodeMap = new HashMap<>();
	private static final Map<ThingsboardErrorCode, HttpStatus> errorCodeToStatusMap = new HashMap<>();

	public static final String YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION = "You don't have permission to perform this operation!";
	public static final String SYSTEM_ERROR = "System error";

	final HttpServletRequest request;

	static {
		statusToErrorCodeMap.put(HttpStatus.BAD_REQUEST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		statusToErrorCodeMap.put(HttpStatus.UNAUTHORIZED, ThingsboardErrorCode.AUTHENTICATION);
		statusToErrorCodeMap.put(HttpStatus.FORBIDDEN, ThingsboardErrorCode.PERMISSION_DENIED);
		statusToErrorCodeMap.put(HttpStatus.NOT_FOUND, ThingsboardErrorCode.NOT_FOUND);
		statusToErrorCodeMap.put(HttpStatus.METHOD_NOT_ALLOWED, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		statusToErrorCodeMap.put(HttpStatus.NOT_ACCEPTABLE, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		statusToErrorCodeMap.put(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		statusToErrorCodeMap.put(HttpStatus.TOO_MANY_REQUESTS, ThingsboardErrorCode.TOO_MANY_REQUESTS);
		statusToErrorCodeMap.put(HttpStatus.INTERNAL_SERVER_ERROR, ThingsboardErrorCode.GENERAL);
		statusToErrorCodeMap.put(HttpStatus.SERVICE_UNAVAILABLE, ThingsboardErrorCode.GENERAL);

		errorCodeToStatusMap.put(ThingsboardErrorCode.GENERAL, HttpStatus.INTERNAL_SERVER_ERROR);
		errorCodeToStatusMap.put(ThingsboardErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED);
		errorCodeToStatusMap.put(ThingsboardErrorCode.JWT_TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED);
		errorCodeToStatusMap.put(ThingsboardErrorCode.CREDENTIALS_EXPIRED, HttpStatus.UNAUTHORIZED);
		errorCodeToStatusMap.put(ThingsboardErrorCode.PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		errorCodeToStatusMap.put(ThingsboardErrorCode.BAD_REQUEST_PARAMS, HttpStatus.BAD_REQUEST);
		errorCodeToStatusMap.put(ThingsboardErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND);
		errorCodeToStatusMap.put(ThingsboardErrorCode.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS);
		errorCodeToStatusMap.put(ThingsboardErrorCode.SUBSCRIPTION_VIOLATION, HttpStatus.FORBIDDEN);
	}

	private static ThingsboardErrorCode statusToErrorCode(HttpStatus status) {
		return statusToErrorCodeMap.getOrDefault(status, ThingsboardErrorCode.GENERAL);
	}

	private static HttpStatus errorCodeToStatus(ThingsboardErrorCode errorCode) {
		return errorCodeToStatusMap.getOrDefault(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping("/error")
	public ResponseEntity<Object> handleError(HttpServletRequest request, HttpServletResponse response) {
		HttpStatus httpStatus = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
			.map(status -> HttpStatus.resolve(Integer.parseInt(status.toString())))
			.orElse(HttpStatus.INTERNAL_SERVER_ERROR);

		Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

		String errorMessage = Optional.ofNullable(throwable)
			.map(e -> (ExceptionUtils.getMessage(e)))
			.orElse(httpStatus.getReasonPhrase());

		logException(request, response, throwable);

		return new ResponseEntity<>(ErrorResponse.of(errorMessage, statusToErrorCode(httpStatus), httpStatus), httpStatus);
	}

	@Override
	@ExceptionHandler(AccessDeniedException.class)
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
		if (response.isCommitted()) {
			return;
		}

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.FORBIDDEN.value());
		JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION, ThingsboardErrorCode.PERMISSION_DENIED, HttpStatus.FORBIDDEN));

		logException(request, response, exception);
	}

	@ExceptionHandler(Exception.class)
	public void handle(Exception exception, HttpServletResponse response) {
		if (response.isCommitted()) {
			return;
		}

		try {
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);

			if (exception instanceof ThingsboardException) {
				ThingsboardException thingsboardException = (ThingsboardException) exception;
				if (thingsboardException.getErrorCode() == ThingsboardErrorCode.SUBSCRIPTION_VIOLATION) {
					handleSubscriptionException((ThingsboardException) exception, response);
				} else {
					handleThingsboardException((ThingsboardException) exception, response);
				}
			} else if (exception instanceof DataAccessException) {
				handleSqlException((DataAccessException) exception, response);
			} else if (exception instanceof RateLimitedException) {
				handleRateLimitException(response, (RateLimitedException) exception);
			} else if (exception instanceof AccessDeniedException) {
				handleAccessDeniedException(response);
			} else if (exception instanceof AuthenticationException) {
				handleAuthenticationException((AuthenticationException) exception, response);
			} else {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of(StringUtils.defaultString(exception.getMessage(), SYSTEM_ERROR), ThingsboardErrorCode.GENERAL, HttpStatus.INTERNAL_SERVER_ERROR));
			}

			logException(request, response, exception);
		} catch (IOException e) {
			log.error("Can't handle exception", e);
		}
	}

	private void logException(HttpServletRequest request, HttpServletResponse response, Throwable throwable) {
		log.error("Processing exception for {}, return http status: {}", request.getRequestURI(), response.getStatus(), throwable);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
															 HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
		if (HttpStatus.INTERNAL_SERVER_ERROR.equals(statusCode)) {
			request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
		}

		log.error("Handle Internal Exception  for http status: {}", statusCode, ex);

		ThingsboardErrorCode errorCode = statusToErrorCode((HttpStatus) statusCode);
		return new ResponseEntity<>(ErrorResponse.of(SYSTEM_ERROR, errorCode, (HttpStatus) statusCode), headers, statusCode);
	}

	private void handleThingsboardException(ThingsboardException thingsboardException, HttpServletResponse response) throws IOException {
		ThingsboardErrorCode errorCode = thingsboardException.getErrorCode();
		HttpStatus status = errorCodeToStatus(errorCode);
		response.setStatus(status.value());
		JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of(thingsboardException.getMessage(), errorCode, status));
	}

	private void handleRateLimitException(HttpServletResponse response, RateLimitedException exception) throws IOException {
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		String message = "Too many requests for current " + exception.getEntityType().name().toLowerCase() + "!";
		JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of(message, ThingsboardErrorCode.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS));
	}

	private void handleSubscriptionException(ThingsboardException subscriptionException, HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.FORBIDDEN.value());
		JacksonUtil.writeValue(response.getWriter(),
			JacksonUtil.fromBytes(((HttpClientErrorException) subscriptionException.getCause()).getResponseBodyAsByteArray(), Object.class));
	}

	private void handleAccessDeniedException(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.FORBIDDEN.value());
		JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION, ThingsboardErrorCode.PERMISSION_DENIED, HttpStatus.FORBIDDEN));
	}

	private void handleSqlException(DataAccessException e, HttpServletResponse response) throws IOException {
		String message = "database error";
		String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
		if (rootCauseMessage != null && rootCauseMessage.contains("Duplicate entry")) {
			final String[] split = rootCauseMessage.split(" ");
			message = split[2] + " already exists";
		}

		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of(message, ThingsboardErrorCode.BAD_REQUEST_PARAMS, HttpStatus.INTERNAL_SERVER_ERROR));
	}

	private void handleAuthenticationException(AuthenticationException authenticationException, HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		if (authenticationException instanceof BadCredentialsException || authenticationException instanceof UsernameNotFoundException) {
			JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of("Invalid username or password", ThingsboardErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
		} else if (authenticationException instanceof DisabledException) {
			JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of("User is not active", ThingsboardErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
		} else if (authenticationException instanceof LockedException) {
			JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of("User is locked due to security policy", ThingsboardErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
		} else if (authenticationException instanceof JwtExpiredTokenException) {
			JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of(authenticationException.getMessage(), ThingsboardErrorCode.JWT_TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED));
		} else if (authenticationException instanceof AuthMethodNotSupportedException || authenticationException instanceof AuthenticationServiceException) {
			JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of(authenticationException.getMessage(), ThingsboardErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
		} else if (authenticationException instanceof UserPasswordExpiredException) {
			UserPasswordExpiredException expiredException = (UserPasswordExpiredException) authenticationException;
			String resetToken = expiredException.getResetToken();
			JacksonUtil.writeValue(response.getWriter(), CredentialsExpiredResponse.of(expiredException.getMessage(), resetToken));
		} else if (authenticationException instanceof UserPasswordNotValidException) {
			UserPasswordNotValidException expiredException = (UserPasswordNotValidException) authenticationException;
			JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of(expiredException.getMessage(), ThingsboardErrorCode.PASSWORD_VIOLATION, HttpStatus.UNAUTHORIZED));
		} else {
			JacksonUtil.writeValue(response.getWriter(), ErrorResponse.of("Authentication failed", ThingsboardErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
		}
	}

}
