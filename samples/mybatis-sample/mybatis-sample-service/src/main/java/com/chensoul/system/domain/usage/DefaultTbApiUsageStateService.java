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
package com.chensoul.system.domain.usage;

import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.notification.channel.mail.MailExecutorService;
import com.chensoul.system.domain.notification.channel.mail.MailService;
import com.chensoul.system.domain.tenant.domain.TenantProfile;
import com.chensoul.system.domain.tenant.domain.TenantProfileConfiguration;
import com.chensoul.system.domain.tenant.domain.TenantProfileData;
import com.chensoul.system.domain.tenant.service.TenantProfileService;
import com.chensoul.system.domain.tenant.service.TenantService;
import com.chensoul.util.concurrent.GroupedThreadFactory;
import com.chensoul.util.date.SchedulerUtils;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    // Entities that should be processed on this server
    final Map<String, BaseApiUsageState> myUsageStates = new ConcurrentHashMap<>();
    // Entities that should be processed on other servers
    final Map<String, ApiUsageState> otherUsageStates = new ConcurrentHashMap<>();
    final Set<String> deletedEntities = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final TenantService tenantService;
    private final ApiUsageStateService apiUsageStateService;
    private final MailService mailService;
    private final MailExecutorService mailExecutor;
    private final TenantProfileService tenantProfileService;
    private final Lock updateLock = new ReentrantLock();
    protected ListeningScheduledExecutorService scheduledExecutor;
    @Value("${usage.stats.report.enabled:true}")
    private boolean enabled;
    @Value("${usage.stats.check.cycle:60000}")
    private long nextCycleCheckInterval;
    @Value("${usage.stats.gauge_report_interval:180000}")
    private long gaugeReportInterval;

    @PostConstruct
    public void init() {
        // Should be always single threaded due to absence of locks.
        scheduledExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor(GroupedThreadFactory.forName(getSchedulerExecutorName())));

        if (enabled) {
            log.info("Starting api usage service");
            scheduledExecutor.scheduleAtFixedRate(this::checkStartOfNextCycle, nextCycleCheckInterval, nextCycleCheckInterval, TimeUnit.MILLISECONDS);
            log.info("Started api usage service");
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
        TenantProfile tenantProfile = tenantProfileService.findTenantProfileByTenantId(tenantId);
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

    private void checkStartOfNextCycle() {
        updateLock.lock();
        try {
            long now = System.currentTimeMillis();
            myUsageStates.values().forEach(state -> {
                if ((state.getNextCycleTs() < now) && (now - state.getNextCycleTs() < TimeUnit.HOURS.toMillis(1))) {
                    state.setCycles(state.getNextCycleTs(), SchedulerUtils.getStartOfNextNextMonth());
                    saveNewCounts(state, Arrays.asList(ApiUsageRecordKey.values()));
                    if (state.getEntityType() == EntityType.TENANT && !state.getEntityId().equals(SYS_TENANT_ID)) {
                        updateTenantState((TenantApiUsageState) state, tenantProfileService.findTenantProfileByTenantId(state.getTenantId()));
                    }
                }
            });
        } finally {
            updateLock.unlock();
        }
    }

    private void saveNewCounts(BaseApiUsageState state, List<ApiUsageRecordKey> keys) {
//		List<TsKvEntry> counts = keys.stream()
//			.map(key -> new BasicTsKvEntry(state.getCurrentCycleTs(), new LongDataEntry(key.getApiCountKey(), 0L)))
//			.collect(Collectors.toList());

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

    private interface StateChecker {
        boolean check(long threshold, long warnThreshold, long value);
    }
}
