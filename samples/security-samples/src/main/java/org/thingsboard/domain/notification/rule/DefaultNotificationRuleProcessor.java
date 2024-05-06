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
package org.thingsboard.domain.notification.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.notification.NotificationCenter;
import org.thingsboard.domain.notification.NotificationExecutorService;
import org.thingsboard.domain.notification.NotificationRequest;
import org.thingsboard.domain.notification.NotificationRequestConfig;
import org.thingsboard.domain.notification.NotificationRequestStatus;
import org.thingsboard.domain.notification.info.NotificationInfo;
import org.thingsboard.domain.notification.persistence.NotificationRequestService;
import org.thingsboard.domain.notification.rule.trigger.NotificationRuleTrigger;
import org.thingsboard.domain.notification.rule.trigger.config.NotificationRuleTriggerConfig;
import org.thingsboard.domain.notification.rule.trigger.config.NotificationRuleTriggerType;
import org.thingsboard.domain.usage.limit.LimitedApi;
import org.thingsboard.domain.usage.limit.RateLimitService;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultNotificationRuleProcessor implements NotificationRuleProcessor {
	private final NotificationRequestService notificationRequestService;
	private final NotificationRuleService notificationRuleService;
	private final NotificationDeduplicationService deduplicationService;
	private final RateLimitService rateLimitService;
	@Autowired
	@Lazy
	private NotificationCenter notificationCenter;
	private final NotificationExecutorService notificationExecutor;

	private final Map<NotificationRuleTriggerType, NotificationRuleTriggerProcessor> triggerProcessors = new EnumMap<>(NotificationRuleTriggerType.class);

	@Autowired
	public void setTriggerProcessors(Collection<NotificationRuleTriggerProcessor> processors) {
		processors.forEach(processor -> {
			triggerProcessors.put(processor.getTriggerType(), processor);
		});
	}

	@Override
	public void process(NotificationRuleTrigger trigger) {
		NotificationRuleTriggerType triggerType = trigger.getType();
		String tenantId = triggerType.isTenantLevel() ? trigger.getTenantId() : SYS_TENANT_ID;
		notificationExecutor.submit(() -> {
			try {
				List<NotificationRule> enabledRules = notificationRuleService.findEnabledNotificationRulesByTenantIdAndTriggerType(tenantId, triggerType);

				if (enabledRules.isEmpty()) {
					return;
				}
				if (trigger.deduplicate()) {
					enabledRules = new ArrayList<>(enabledRules);
					enabledRules.removeIf(rule -> deduplicationService.alreadyProcessed(trigger, rule));
				}
				final List<NotificationRule> rules = enabledRules;
				for (NotificationRule rule : rules) {
					try {
						processNotificationRule(rule, trigger);
					} catch (Throwable e) {
						log.error("Failed to process notification rule {} for trigger type {} with trigger object {}", rule.getId(), rule.getTriggerType(), trigger, e);
					}
				}
			} catch (Throwable e) {
				log.error("Failed to process notification rules for trigger: {}", trigger, e);
			}
		});
	}

	private void processNotificationRule(NotificationRule rule, NotificationRuleTrigger trigger) {
		NotificationRuleTriggerConfig triggerConfig = rule.getTriggerConfig();
		log.debug("Processing notification rule '{}' for trigger type {}", rule.getName(), rule.getTriggerType());

		if (matchesClearRule(trigger, triggerConfig)) {
			List<NotificationRequest> notificationRequests = findAlreadySentNotificationRequests(rule, trigger);
			if (notificationRequests.isEmpty()) {
				return;
			}

			List<Long> targets = notificationRequests.stream()
				.filter(NotificationRequest::isSent)
				.flatMap(notificationRequest -> notificationRequest.getTargets().stream())
				.distinct().collect(Collectors.toList());
			NotificationInfo notificationInfo = constructNotificationInfo(trigger, triggerConfig);
			submitNotificationRequest(targets, rule, trigger.getOriginatorEntityId(), notificationInfo, 0);

			notificationRequests.forEach(notificationRequest -> {
				if (notificationRequest.isScheduled()) {
					notificationCenter.deleteNotificationRequest(notificationRequest.getId());
				}
			});
			return;
		}

		if (matchesFilter(trigger, triggerConfig)) {
			if (!rateLimitService.checkRateLimited(LimitedApi.NOTIFICATION_REQUESTS_PER_RULE, rule.getTenantId(), rule.getId())) {
				log.debug("[{}] Rate limit for notification requests per rule was exceeded (rule '{}')", rule.getTenantId(), rule.getName());
				return;
			}

			NotificationInfo notificationInfo = constructNotificationInfo(trigger, triggerConfig);
			rule.getRecipientsConfig().getTargetsTable().forEach((delay, targets) -> {
				submitNotificationRequest(targets, rule, trigger.getOriginatorEntityId(), notificationInfo, delay);
			});
		}
	}

	private List<NotificationRequest> findAlreadySentNotificationRequests(NotificationRule rule, NotificationRuleTrigger trigger) {
		return notificationRequestService.findNotificationRequestsByRuleIdAndOriginatorEntityId(rule.getId(), trigger.getOriginatorEntityId());
	}

	private void submitNotificationRequest(List<Long> targets, NotificationRule rule,
										   Serializable originatorEntityId, NotificationInfo notificationInfo, int delayInSec) {
		NotificationRequestConfig config = new NotificationRequestConfig();
		if (delayInSec > 0) {
			config.setSendingDelayInSec(delayInSec);
		}
		NotificationRequest notificationRequest = NotificationRequest.builder()
			.tenantId(rule.getTenantId())
			.targets(targets)
			.templateId(rule.getTemplateId())
			.config(config)
			.info(notificationInfo)
			.ruleId(rule.getId())
			.entityId(originatorEntityId)
			.build();

		try {
			log.debug("Submitting notification request for rule '{}' with delay of {} sec to targets {}", rule.getName(), delayInSec, targets);
			notificationCenter.processNotificationRequest(notificationRequest, null);
		} catch (Exception e) {
			log.error("Failed to process notification request for tenant {} for rule {}", rule.getTenantId(), rule.getId(), e);
		}
	}

	private boolean matchesFilter(NotificationRuleTrigger trigger, NotificationRuleTriggerConfig triggerConfig) {
		return triggerProcessors.get(triggerConfig.getTriggerType()).matchesFilter(trigger, triggerConfig);
	}

	private boolean matchesClearRule(NotificationRuleTrigger trigger, NotificationRuleTriggerConfig triggerConfig) {
		return triggerProcessors.get(triggerConfig.getTriggerType()).matchesClearRule(trigger, triggerConfig);
	}

	private NotificationInfo constructNotificationInfo(NotificationRuleTrigger trigger, NotificationRuleTriggerConfig triggerConfig) {
		return triggerProcessors.get(triggerConfig.getTriggerType()).constructNotificationInfo(trigger);
	}

//	@EventListener(ComponentLifecycleMsg.class)
//	public void onNotificationRuleDeleted(ComponentLifecycleMsg componentLifecycleMsg) {
//		if (componentLifecycleMsg.getEvent() != ComponentLifecycleEvent.DELETED ||
//			componentLifecycleMsg.getEntityId().getEntityType() != EntityType.NOTIFICATION_RULE) {
//			return;
//		}
//
//		TenantId tenantId = componentLifecycleMsg.getTenantId();
//		Long notificationRuleId = (NotificationRuleId) componentLifecycleMsg.getEntityId();
//		if (partitionService.isMyPartition(ServiceType.TB_CORE, tenantId, notificationRuleId)) {
//			notificationExecutor.submit(() -> {
//				List<NotificationRequestId> scheduledForRule = notificationRequestService.findNotificationRequestsIdsByStatusAndRuleId(tenantId, NotificationRequestStatus.SCHEDULED, notificationRuleId);
//				for (NotificationRequestId notificationRequestId : scheduledForRule) {
//					notificationCenter.deleteNotificationRequest(tenantId, notificationRequestId);
//				}
//			});
//		}
//	}


}
