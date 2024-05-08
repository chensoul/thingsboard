package org.thingsboard.domain.audit.internal.config;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class AuditAspect {
	@Around("execution(* org.thingsboard.domain..controller..*.*(..))")
	public Object handleAudit(final ProceedingJoinPoint joinPoint) throws Throwable {
		final Logger log = logger(joinPoint);
		if (log.isDebugEnabled()) {
			log.debug("Enter: {}() with argument[s] = {}", joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
		}

		final Long startTime = System.currentTimeMillis();

		String principal = null, actionType = null, failDetail = null;
		String[] auditResource = new String[]{null};
		Object retVal = null;
		try {
			retVal = joinPoint.proceed();

			if (log.isDebugEnabled()) {
				log.debug("Exit: {}() with result = {}", joinPoint.getSignature().getName(), retVal);
			}
			return retVal;
		} catch (final Throwable t) {
			throw t;
		} finally {
			final long cost = System.currentTimeMillis() - startTime;
//			doAudit(joinPoint, principal, actionType, auditResource, retVal, actionStatus, failDetail, cost);
		}
	}

	private static Logger logger(final JoinPoint joinPoint) {
		return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
	}
}
