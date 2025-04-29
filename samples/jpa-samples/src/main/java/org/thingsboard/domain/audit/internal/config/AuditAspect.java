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
