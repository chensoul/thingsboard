package org.thingsboard.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.thingsboard.common.ExceptionConstants.YOU_AREN_T_AUTHORIZED_TO_PERFORM_THIS_OPERATION;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class SecurityUtils {
	public static SecurityUser getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
			return (SecurityUser) authentication.getPrincipal();
		} else {
			throw new ThingsboardException(YOU_AREN_T_AUTHORIZED_TO_PERFORM_THIS_OPERATION, ThingsboardErrorCode.AUTHENTICATION);
		}
	}

	public static String getTenantId() {
		return getCurrentUser().getTenantId();
	}

	public static Long getUserId() {
		return getCurrentUser().getId();
	}

	public static boolean isSysTenantId(String tenantId) {
		return tenantId == null || tenantId.equals(SYS_TENANT_ID);
	}

}
