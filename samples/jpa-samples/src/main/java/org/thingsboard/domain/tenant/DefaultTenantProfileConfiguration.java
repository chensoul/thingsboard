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
package org.thingsboard.domain.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.usage.ApiUsageRecordKey;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DefaultTenantProfileConfiguration implements TenantProfileConfiguration {

	private static final long serialVersionUID = -7134932690332578595L;

	private long maxDevices;
	private long maxAssets;
	private long maxCustomers;
	private long maxUsers;
	private long maxDashboards;
	private long maxRuleChains;
	private long maxResourcesInBytes;
	private long maxOtaPackagesInBytes;
	private long maxResourceSize;

	private String transportTenantMsgRateLimit;
	private String transportTenantTelemetryMsgRateLimit;
	private String transportTenantTelemetryDataPointsRateLimit;
	private String transportDeviceMsgRateLimit;
	private String transportDeviceTelemetryMsgRateLimit;
	private String transportDeviceTelemetryDataPointsRateLimit;

	private String tenantEntityExportRateLimit;
	private String tenantEntityImportRateLimit;
	private String tenantNotificationRequestsRateLimit;
	private String tenantNotificationRequestsPerRuleRateLimit;

	private String tenantServerRestLimit;
	private String customerServerRestLimit;

	private long maxTransportMessages;
	private long maxTransportDataPoints;
	private long maxREExecutions;
	private long maxJSExecutions;
	private long maxTbelExecutions;
	private long maxDPStorageDays;
	private int maxRuleNodeExecutionsPerMessage;
	private long maxEmails;
	private Boolean smsEnabled;
	private long maxSms;
	private long maxCreatedAlarms;

	private int maxWsSessionsPerTenant;
	private int maxWsSessionsPerCustomer;
	private int maxWsSessionsPerRegularUser;
	private int maxWsSessionsPerPublicUser;
	private int wsMsgQueueLimitPerSession;
	private long maxWsSubscriptionsPerTenant;
	private long maxWsSubscriptionsPerCustomer;
	private long maxWsSubscriptionsPerRegularUser;
	private long maxWsSubscriptionsPerPublicUser;
	private String wsUpdatesPerSessionRateLimit;

	private String cassandraQueryTenantRateLimitsConfiguration;

	private String edgeEventRateLimits;
	private String edgeEventRateLimitsPerEdge;
	private String edgeUplinkMessagesRateLimits;
	private String edgeUplinkMessagesRateLimitsPerEdge;

	private int defaultStorageTtlDays;
	private int alarmsTtlDays;
	private int rpcTtlDays;
	private int queueStatsTtlDays;
	private int ruleEngineExceptionsTtlDays;

	private double warnThreshold;

	public long getProfileThreshold(ApiUsageRecordKey key) {
		switch (key) {
			case TRANSPORT_MSG_COUNT:
				return maxTransportMessages;
			case TRANSPORT_DP_COUNT:
				return maxTransportDataPoints;
			case JS_EXEC_COUNT:
				return maxJSExecutions;
			case TBEL_EXEC_COUNT:
				return maxTbelExecutions;
			case RE_EXEC_COUNT:
				return maxREExecutions;
			case STORAGE_DP_COUNT:
				return maxDPStorageDays;
			case EMAIL_EXEC_COUNT:
				return maxEmails;
			case SMS_EXEC_COUNT:
				return maxSms;
			case CREATED_ALARMS_COUNT:
				return maxCreatedAlarms;
		}
		return 0L;
	}

	public long getEntitiesLimit(EntityType entityType) {
		switch (entityType) {
			case MERCHANT:
				return maxCustomers;
			case USER:
				return maxUsers;
			default:
				return 0;
		}
	}

	public boolean getProfileFeatureEnabled(ApiUsageRecordKey key) {
		switch (key) {
			case SMS_EXEC_COUNT:
				return smsEnabled == null || Boolean.TRUE.equals(smsEnabled);
			default:
				return true;
		}
	}

	public long getWarnThreshold(ApiUsageRecordKey key) {
		return (long) (getProfileThreshold(key) * (warnThreshold > 0.0 ? warnThreshold : 0.8));
	}

	@Override
	public TenantProfileType getType() {
		return TenantProfileType.DEFAULT;
	}

	@Override
	public int getMaxRuleNodeExecsPerMessage() {
		return maxRuleNodeExecutionsPerMessage;
	}
}
