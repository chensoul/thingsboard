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
package org.thingsboard.domain.usage;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.util.SchedulerUtils;
import org.thingsboard.common.util.ThingsBoardThreadFactory;
import org.thingsboard.domain.kv.BasicTsKvEntry;
import org.thingsboard.domain.kv.LongDataEntry;
import org.thingsboard.domain.kv.StringDataEntry;
import org.thingsboard.domain.kv.TsKvEntry;
import org.thingsboard.domain.notification.channel.mail.MailExecutorService;
import org.thingsboard.domain.notification.channel.mail.MailService;
import org.thingsboard.domain.tenant.model.TenantProfile;
import org.thingsboard.domain.tenant.model.TenantProfileConfiguration;
import org.thingsboard.domain.tenant.model.TenantProfileData;
import org.thingsboard.domain.tenant.service.TenantProfileService;
import org.thingsboard.domain.tenant.service.TenantService;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultTbApiUsageStateService implements TbApiUsageStateService {

	public static final String HOURLY = "Hourly";
	public static final FutureCallback<Integer> VOID_CALLBACK = new FutureCallback<Integer>() {
		@Override
		public void onSuccess(@Nullable Integer result) {
		}

		@Override
		public void onFailure(Throwable t) {
		}
	};
	private final TenantService tenantService;
	private final ApiUsageStateService apiUsageStateService;
	private final MailService mailService;
	private final MailExecutorService mailExecutor;
	private final TenantProfileService tenantProfileService;

	// Entities that should be processed on this server
	final Map<String, BaseApiUsageState> myUsageStates = new ConcurrentHashMap<>();
	// Entities that should be processed on other servers
	final Map<String, ApiUsageState> otherUsageStates = new ConcurrentHashMap<>();

	final Set<String> deletedEntities = Collections.newSetFromMap(new ConcurrentHashMap<>());

	protected ListeningScheduledExecutorService scheduledExecutor;

	@Value("${usage.stats.report.enabled:true}")
	private boolean enabled;

	@Value("${usage.stats.check.cycle:60000}")
	private long nextCycleCheckInterval;

	@Value("${usage.stats.gauge_report_interval:180000}")
	private long gaugeReportInterval;

	private final Lock updateLock = new ReentrantLock();

	@PostConstruct
	public void init() {
		// Should be always single threaded due to absence of locks.
		scheduledExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor(ThingsBoardThreadFactory.forName(getSchedulerExecutorName())));

		if (enabled) {
			log.info("Starting api usage service.");
			scheduledExecutor.scheduleAtFixedRate(this::checkStartOfNextCycle, nextCycleCheckInterval, nextCycleCheckInterval, TimeUnit.MILLISECONDS);
			log.info("Started api usage service.");
		}
	}

	protected String getServiceName() {
		return "API Usage";
	}

	protected String getSchedulerExecutorName() {
		return "api-usage-scheduled";
	}

	@Override
	public ApiUsageState getApiUsageState(String tenantId) {
		TenantApiUsageState tenantState = (TenantApiUsageState) myUsageStates.get(tenantId);
		if (tenantState != null) {
			return tenantState.getApiUsageState();
		} else {
			ApiUsageState state = otherUsageStates.get(tenantId);
			if (state != null) {
				return state;
			} else {
//				if (partitionService.resolve(ServiceType.TB_CORE, tenantId, tenantId).isMyPartition()) {
//					return getOrFetchState(tenantId, tenantId).getApiUsageState();
//				} else {
				state = otherUsageStates.get(tenantId);
				if (state == null) {
					state = apiUsageStateService.findTenantApiUsageState(tenantId);
					if (state != null) {
						otherUsageStates.put(tenantId, state);
					}
				}
				return state;
//				}
			}
		}
	}

	@Override
	public void onApiUsageStateUpdate(String tenantId) {
		otherUsageStates.remove(tenantId);
	}

	@Override
	public void onTenantProfileUpdate(Long tenantProfileId) {
		log.info("[{}] On Tenant Profile Update", tenantProfileId);
		TenantProfile tenantProfile = null;//tenantProfileCache.get(tenantProfileId);
		updateLock.lock();
		try {
			myUsageStates.values().stream()
				.filter(state -> state.getEntityType() == EntityType.TENANT)
				.map(state -> (TenantApiUsageState) state)
				.forEach(state -> {
					if (tenantProfile.getId().equals(state.getTenantProfileId())) {
						updateTenantState(state, tenantProfile);
					}
				});
		} finally {
			updateLock.unlock();
		}
	}

	@Override
	public void onTenantUpdate(String tenantId) {
		log.info("[{}] On Tenant Update.", tenantId);
		TenantProfile tenantProfile = tenantProfileService.findDefaultTenantProfile();
		updateLock.lock();
		try {
			TenantApiUsageState state = (TenantApiUsageState) myUsageStates.get(tenantId);
			if (state != null && !state.getTenantProfileId().equals(tenantProfile.getId())) {
				updateTenantState(state, tenantProfile);
			}
		} finally {
			updateLock.unlock();
		}
	}

	private void updateTenantState(TenantApiUsageState state, TenantProfile profile) {
		TenantProfileData oldProfileData = state.getTenantProfileData();
		state.setTenantProfileId(profile.getId());
		state.setTenantProfileData(profile.getProfileData());
		Map<ApiFeature, ApiUsageStateValue> result = state.checkStateUpdatedDueToThresholds();
		if (!result.isEmpty()) {
			persistAndNotify(state, result);
		}
		updateProfileThresholds(state.getTenantId(), state.getApiUsageState().getId(),
			oldProfileData.getConfiguration(), profile.getProfileData().getConfiguration());
	}

	private void updateProfileThresholds(String tenantId, Long id,
										 TenantProfileConfiguration oldData, TenantProfileConfiguration newData) {
		long ts = System.currentTimeMillis();
		List<TsKvEntry> profileThresholds = new ArrayList<>();
		for (ApiUsageRecordKey key : ApiUsageRecordKey.values()) {
			long newProfileThreshold = newData.getProfileThreshold(key);
			if (oldData == null || oldData.getProfileThreshold(key) != newProfileThreshold) {
				log.info("[{}] Updating profile threshold [{}]:[{}]", tenantId, key, newProfileThreshold);
				profileThresholds.add(new BasicTsKvEntry(ts, new LongDataEntry(key.getApiLimitKey(), newProfileThreshold)));
			}
		}
		if (!profileThresholds.isEmpty()) {
//			tsWsService.saveAndNotifyInternal(tenantId, id, profileThresholds, VOID_CALLBACK);
		}
	}

	public void onTenantDelete(String tenantId) {
		deletedEntities.add(tenantId);
		myUsageStates.remove(tenantId);
		otherUsageStates.remove(tenantId);
	}

	@Override
	public void onCustomerDelete(Long customerId) {
		deletedEntities.add(String.valueOf(customerId));
		myUsageStates.remove(customerId);
	}

	protected void cleanupEntityOnPartitionRemoval(String entityId) {
		myUsageStates.remove(entityId);
	}

	private void persistAndNotify(BaseApiUsageState state, Map<ApiFeature, ApiUsageStateValue> result) {
		log.info("[{}] Detected update of the API state for {}: {}", state.getEntityId(), state.getEntityType(), result);
		apiUsageStateService.update(state.getApiUsageState());
		long ts = System.currentTimeMillis();
		List<TsKvEntry> stateTelemetry = new ArrayList<>();
		result.forEach((apiFeature, aState) -> stateTelemetry.add(new BasicTsKvEntry(ts, new StringDataEntry(apiFeature.getApiStateKey(), aState.name()))));
//		tsWsService.saveAndNotifyInternal(state.getTenantId(), state.getApiUsageState().getId(), stateTelemetry, VOID_CALLBACK);

		if (state.getEntityType() == EntityType.TENANT && !state.getEntityId().equals(SYS_TENANT_ID)) {
			String email = tenantService.findTenantById(state.getTenantId()).getEmail();
			result.forEach((apiFeature, stateValue) -> {
				ApiUsageRecordState recordState = createApiUsageRecordState((TenantApiUsageState) state, apiFeature, stateValue);
				if (recordState == null) {
					return;
				}
//				notificationRuleProcessor.process(ApiUsageLimitTrigger.builder()
//					.tenantId(state.getTenantId())
//					.state(recordState)
//					.status(stateValue)
//					.build());
				if (StringUtils.isNotEmpty(email)) {
					mailExecutor.submit(() -> {
						try {
//							mailService.sendApiFeatureStateEmail(apiFeature, stateValue, email, recordState);
						} catch (ThingsboardException e) {
							log.warn("[{}] Can't send update of the API state to tenant with provided email [{}]", state.getTenantId(), email, e);
						}
					});
				}
			});
		}
	}

	private ApiUsageRecordState createApiUsageRecordState(TenantApiUsageState state, ApiFeature apiFeature, ApiUsageStateValue stateValue) {
		StateChecker checker = getStateChecker(stateValue);
		for (ApiUsageRecordKey apiUsageRecordKey : ApiUsageRecordKey.getKeys(apiFeature)) {
			long threshold = state.getProfileThreshold(apiUsageRecordKey);
			long warnThreshold = state.getProfileWarnThreshold(apiUsageRecordKey);
			long value = state.get(apiUsageRecordKey);
			if (checker.check(threshold, warnThreshold, value)) {
				return new ApiUsageRecordState(apiFeature, apiUsageRecordKey, threshold, value);
			}
		}
		return null;
	}

	private StateChecker getStateChecker(ApiUsageStateValue stateValue) {
		if (ApiUsageStateValue.ENABLED.equals(stateValue)) {
			return (t, wt, v) -> true;
		} else if (ApiUsageStateValue.WARNING.equals(stateValue)) {
			return (t, wt, v) -> v < t && v >= wt;
		} else {
			return (t, wt, v) -> t > 0 && v >= t;
		}
	}

	public ApiUsageState findApiUsageStateById(String tenantId, Long id) {
		return apiUsageStateService.findApiUsageStateById(tenantId, id);
	}

	private interface StateChecker {
		boolean check(long threshold, long warnThreshold, long value);
	}

	private void checkStartOfNextCycle() {
		updateLock.lock();
		try {
			long now = System.currentTimeMillis();
			myUsageStates.values().forEach(state -> {
				if ((state.getNextCycleTs() < now) && (now - state.getNextCycleTs() < TimeUnit.HOURS.toMillis(1))) {
					state.setCycles(state.getNextCycleTs(), SchedulerUtils.getStartOfNextNextMonth());
					saveNewCounts(state, Arrays.asList(ApiUsageRecordKey.values()));
					if (state.getEntityType() == EntityType.TENANT && !state.getEntityId().equals(SYS_TENANT_ID)) {
						updateTenantState((TenantApiUsageState) state, tenantProfileService.findDefaultTenantProfile());
					}
				}
			});
		} finally {
			updateLock.unlock();
		}
	}

	private void saveNewCounts(BaseApiUsageState state, List<ApiUsageRecordKey> keys) {
		List<TsKvEntry> counts = keys.stream()
			.map(key -> new BasicTsKvEntry(state.getCurrentCycleTs(), new LongDataEntry(key.getApiCountKey(), 0L)))
			.collect(Collectors.toList());

//		tsWsService.saveAndNotifyInternal(state.getTenantId(), state.getApiUsageState().getId(), counts, VOID_CALLBACK);
	}

	protected void onRepartitionEvent() {
//		otherUsageStates.entrySet().removeIf(entry ->
//			partitionService.resolve(ServiceType.TB_CORE, entry.getValue().getTenantId(), entry.getKey()).isMyPartition());
		updateLock.lock();
		try {
			myUsageStates.values().forEach(BaseApiUsageState::onRepartitionEvent);
		} finally {
			updateLock.unlock();
		}
	}

	private void destroy() {
		if (scheduledExecutor != null) {
			scheduledExecutor.shutdownNow();
		}
	}
}
