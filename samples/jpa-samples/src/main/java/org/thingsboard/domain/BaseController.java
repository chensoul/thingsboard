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
package org.thingsboard.domain;

import jakarta.mail.MessagingException;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.util.function.ThrowingFunction;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.common.exception.IncorrectParameterException;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.model.HasId;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Validator;
import static org.thingsboard.common.validation.Validator.validateId;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.audit.AuditLogService;
import org.thingsboard.domain.iot.device.Device;
import org.thingsboard.domain.iot.device.DeviceService;
import org.thingsboard.domain.iot.deviceprofile.DeviceProfile;
import org.thingsboard.domain.iot.deviceprofile.DeviceProfileService;
import org.thingsboard.domain.usage.limit.RateLimitService;
import org.thingsboard.domain.setting.SecuritySettingService;
import org.thingsboard.domain.merchant.Merchant;
import org.thingsboard.domain.tenant.Tenant;
import org.thingsboard.domain.tenant.TenantInfo;
import org.thingsboard.domain.tenant.TenantProfile;
import org.thingsboard.domain.merchant.MerchantService;
import org.thingsboard.domain.tenant.TenantProfileService;
import org.thingsboard.domain.tenant.TenantService;
import org.thingsboard.domain.user.User;
import org.thingsboard.domain.user.UserService;
import org.thingsboard.domain.user.UserSettingService;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.permission.AccessControlService;
import org.thingsboard.server.security.permission.Operation;
import org.thingsboard.server.security.permission.Resource;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
public class BaseController {
	@Autowired
	protected AccessControlService accessControlService;

	@Autowired
	protected UserService userService;

	@Autowired
	protected UserSettingService userSettingService;

	@Autowired
	protected SecuritySettingService securitySettingService;

	@Autowired
	protected RateLimitService rateLimitService;

	@Autowired
	protected MerchantService merchantService;

	@Autowired
	protected TenantService tenantService;

	@Autowired
	protected TenantProfileService tenantProfileService;

	@Autowired
	protected DeviceService deviceService;

	@Autowired
	protected DeviceProfileService deviceProfileService;

	@Autowired
	private AuditLogService auditLogService;

	protected <E extends HasId<I> & HasTenantId, I extends Serializable> E checkEntity(E entity, EntityType entityType, Operation operation) {
		Validator.checkNotNull(entity, "Entity not found");
		accessControlService.checkPermission(getCurrentUser(), Resource.of(entityType), operation, entity.getId(), entity);
		return entity;
	}

	protected <E extends HasId<I> & HasTenantId, I extends Serializable> E checkEntityId(I entityId, ThrowingFunction<I, E> findingFunction, EntityType entityType, Operation operation) {
		if (entityId == null) {
			return null;
		}
		try {
			validateId(entityId, "Invalid entity id");
			E entity = findingFunction.apply(entityId);
			Validator.checkNotNull(entity, "Item [" + entityId + "] is not found");

			return checkEntity(entity, entityType, operation);
		} catch (Exception e) {
			throw handleException(e, false);
		}
	}

	protected <I extends Serializable, T extends BaseData<I> & HasTenantId> void checkEntity(T entity, EntityType entityType) throws ThingsboardException {
		if (entity.getId() == null) {
			accessControlService.checkPermission(getCurrentUser(), Resource.of(entityType), Operation.CREATE, null, entity);
		} else {
			checkEntityId(entity.getId(), entityType, Operation.WRITE);
		}
	}

	protected void checkEntityId(Serializable entityId, EntityType entityType, Operation operation) {
		try {
			if (entityId == null) {
				throw new ThingsboardException("Parameter entityId can't be empty!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
			}
			validateId(entityId, id -> "Incorrect entityId " + id);
			switch (entityType) {
				case MERCHANT:
					checkMerchantId((Long) entityId, operation);
					return;
				case TENANT:
					checkTenantId((String) entityId, operation);
					return;
				case TENANT_PROFILE:
					checkTenantProfileId((Long) entityId, operation);
					return;
				case USER:
					checkUserId((Long) entityId, operation);
					return;
				default:
					return;
			}
		} catch (Exception e) {
			throw handleException(e, false);
		}
	}

	protected Merchant checkMerchantId(Long merchantId, Operation operation) {
		return checkEntityId(merchantId, merchantService::findMerchantById, EntityType.MERCHANT, operation);
	}

	protected User checkUserId(Long userId, Operation operation) {
		return checkEntityId(userId, userService::findUserById, EntityType.USER, operation);
	}

	protected Tenant checkTenantId(String tenantId, Operation operation) {
		return checkEntityId(tenantId, tenantService::findTenantById, EntityType.TENANT, operation);
	}

	protected TenantInfo checkTenantInfoId(String tenantId, Operation operation) {
		return checkEntityId(tenantId, tenantService::findTenantInfoById, EntityType.TENANT, operation);
	}

	protected TenantProfile checkTenantProfileId(Long tenantProfileId, Operation operation) throws ThingsboardException {
		try {
			validateId(tenantProfileId, id -> "Incorrect tenantProfileId " + id);
			TenantProfile tenantProfile = tenantProfileService.findTenantProfileById(tenantProfileId);
			Validator.checkNotNull(tenantProfile, "Tenant profile with id [" + tenantProfileId + "] is not found");
			accessControlService.checkPermission(getCurrentUser(), Resource.TENANT_PROFILE, operation);
			return tenantProfile;
		} catch (Exception e) {
			throw handleException(e, false);
		}
	}

	protected Device checkDeviceId(String deviceId, Operation operation) throws ThingsboardException {
		return checkEntityId(deviceId, deviceService::findDeviceById, EntityType.DEVICE, operation);
	}

	protected DeviceProfile checkDeviceProfileId(Long deviceProfileId, Operation operation) throws ThingsboardException {
		return checkEntityId(deviceProfileId, deviceProfileService::findDeviceProfileById, EntityType.DEVICE, operation);
	}

	protected ThingsboardException handleException(Exception exception, boolean logException) {
		if (logException) {
			log.error("Error [{}]", exception.getMessage(), exception);
		}

		String cause = "";
		if (exception.getCause() != null) {
			cause = exception.getCause().getClass().getCanonicalName();
		}

		if (exception instanceof ThingsboardException) {
			return (ThingsboardException) exception;
		} else if (exception instanceof IllegalArgumentException || exception instanceof DataValidationException
				   || exception instanceof IncorrectParameterException || cause.contains("IncorrectParameterException")) {
			return new ThingsboardException(exception.getMessage(), ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		} else if (exception instanceof MessagingException) {
			return new ThingsboardException("Unable to send mail: " + exception.getMessage(), ThingsboardErrorCode.GENERAL);
		} else if (exception instanceof AsyncRequestTimeoutException) {
			return new ThingsboardException("Request timeout", ThingsboardErrorCode.GENERAL);
		} else if (exception instanceof DataAccessException) {
			String errorType = exception.getClass().getSimpleName();
			if (logException) {
				log.warn("Database error: {} - {}", errorType, ExceptionUtils.getRootCauseMessage(exception));
			}
			return new ThingsboardException("Database error", ThingsboardErrorCode.GENERAL);
		}
		return new ThingsboardException(exception.getMessage(), exception, ThingsboardErrorCode.GENERAL);
	}

	protected <E extends BaseData<? extends Serializable>> void logEntityAction(SecurityUser user, E requestEntity, E oldEntity, E savedEntity, EntityType entityType, ActionType actionType) {
		logEntityAction(user, requestEntity, oldEntity, savedEntity, entityType, actionType, null);
	}

	protected <E extends BaseData<? extends Serializable>> void logEntityAction(SecurityUser user, E requestEntity, E oldEntity, E savedEntity, EntityType entityType, ActionType actionType, Exception e) {
		auditLogService.logEntityAction(user, requestEntity, oldEntity, savedEntity, entityType, actionType, e, null);
	}

	protected <E extends BaseData<? extends Serializable>> E doSaveAndLog(E requestEntity, E oldEntity, EntityType entityType, Function<E, E> savingFunction) throws Exception {
		ActionType actionType = requestEntity.getId() == null ? ActionType.ADD : ActionType.UPDATE;
		SecurityUser user = getCurrentUser();
		try {
			E savedEntity = savingFunction.apply(requestEntity);
			logEntityAction(user, requestEntity, oldEntity, savedEntity, entityType, actionType);
			return savedEntity;
		} catch (Exception e) {
			logEntityAction(user, requestEntity, oldEntity, null, entityType, actionType, e);
			throw e;
		}
	}

	protected <E extends BaseData<I>, I extends Serializable> void doDeleteAndLog(E oldEntity, EntityType entityType, Consumer<I> deleteFunction) throws Exception {
		SecurityUser user = getCurrentUser();
		try {
			deleteFunction.accept(oldEntity.getId());
			logEntityAction(user, oldEntity, oldEntity, null, entityType, ActionType.DELETE);
		} catch (Exception e) {
			logEntityAction(user, oldEntity, oldEntity, null, entityType, ActionType.DELETE, e);
			throw e;
		}
	}

}
