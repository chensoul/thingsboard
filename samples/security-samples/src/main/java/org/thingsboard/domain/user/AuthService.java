package org.thingsboard.domain.user;

import jakarta.servlet.http.HttpServletRequest;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface AuthService {
	void sendActivationEmail(String email, HttpServletRequest request);

	String getActivationLink(Long userId, HttpServletRequest request);
}
