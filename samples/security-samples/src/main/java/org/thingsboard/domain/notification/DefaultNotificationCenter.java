package org.thingsboard.domain.notification;

import com.google.common.util.concurrent.FutureCallback;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.common.exception.RateLimitedException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.notification.channel.NotificationChannel;
import org.thingsboard.domain.notification.persistence.NotificationRequestService;
import org.thingsboard.domain.notification.persistence.NotificationTargetService;
import org.thingsboard.domain.notification.persistence.NotificationTemplateService;
import org.thingsboard.domain.setting.notification.NotificationSetting;
import org.thingsboard.domain.notification.targets.NotificationTarget;
import org.thingsboard.domain.notification.targets.PlatformUserNotificationTargetConfig;
import org.thingsboard.domain.notification.template.NotificationDeliveryTemplate;
import org.thingsboard.domain.notification.template.NotificationDeliveryMethod;
import org.thingsboard.domain.notification.template.NotificationTemplate;
import org.thingsboard.domain.notification.template.WebNotificationDeliveryTemplate;
import org.thingsboard.domain.setting.notification.NotificationSettingService;
import org.thingsboard.domain.usage.limit.LimitedApi;
import org.thingsboard.domain.usage.limit.RateLimitService;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;
import org.thingsboard.server.security.SecurityUtils;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultNotificationCenter implements NotificationChannel<User, WebNotificationDeliveryTemplate>, NotificationCenter {
	private Map<NotificationDeliveryMethod, NotificationChannel> channels;

	private final NotificationExecutorService notificationExecutor;
	private final NotificationTargetService notificationTargetService;

	private final RateLimitService rateLimitService;
	private final NotificationTemplateService notificationTemplateService;
	private final NotificationRequestService notificationRequestService;
	private final NotificationSettingService notificationSettingService;


	@Autowired
	public void setChannels(List<NotificationChannel> channels, DefaultNotificationCenter webNotificationChannel) {
		this.channels = channels.stream().collect(Collectors.toMap(NotificationChannel::getDeliveryMethod, c -> c));
		this.channels.put(NotificationDeliveryMethod.WEB, (NotificationChannel) webNotificationChannel);
	}

	@Override
	public NotificationRequest processNotificationRequest(NotificationRequest request, FutureCallback<NotificationRequestStats> callback) {
		if (request.getRuleId() == null) {
			if (!rateLimitService.checkRateLimited(LimitedApi.NOTIFICATION_REQUESTS, request.getTenantId())) {
				throw new RateLimitedException(EntityType.TENANT);
			}
		}

		NotificationTemplate notificationTemplate;
		if (request.getTemplateId() != null) {
			notificationTemplate = notificationTemplateService.findNotificationTemplateById(request.getTemplateId());
		} else {
			notificationTemplate = request.getTemplate();
		}
		if (notificationTemplate == null) throw new IllegalArgumentException("Template is missing");

		Set<NotificationDeliveryMethod> deliveryMethods = new HashSet<>();
		List<NotificationTarget> targets = request.getTargets().stream()
			.map(id -> notificationTargetService.findNotificationTargetById(id))
			.collect(Collectors.toList());

		String tenantId = request.getTenantId();
		Long ruleId = request.getRuleId();
		notificationTemplate.getConfig().getDeliveryTemplates().forEach((deliveryMethod, template) -> {
			if (!template.isEnabled()) return;
			try {
				channels.get(deliveryMethod).check(tenantId);
			} catch (Exception e) {
				if (ruleId == null) {
					throw new IllegalArgumentException(e.getMessage());
				} else {
					return; // if originated by rule - just ignore delivery method
				}
			}
			if (ruleId == null) {
				if (targets.stream().noneMatch(target -> target.getConfig().getType().getSupportedDeliveryTypes().contains(deliveryMethod))) {
					throw new IllegalArgumentException("Recipients for " + deliveryMethod.getName() + " delivery method not chosen");
				}
			}
			deliveryMethods.add(deliveryMethod);
		});
		if (deliveryMethods.isEmpty()) {
			throw new IllegalArgumentException("No delivery methods to send notification with");
		}

		if (request.getConfig() != null) {
			NotificationRequestConfig config = request.getConfig();
			if (config.getSendingDelayInSec() > 0 && request.getId() == null) {
				request.setStatus(NotificationRequestStatus.SCHEDULED);
				request = notificationRequestService.saveNotificationRequest(request);
				forwardToNotificationSchedulerService(tenantId, request.getId());
				return request;
			}
		}
		NotificationSetting settings = notificationSettingService.findNotificationSetting(tenantId);
		NotificationSetting systemSettings = SecurityUtils.isSysTenantId(tenantId) ? settings : notificationSettingService.findNotificationSetting(SYS_TENANT_ID);

		log.debug("Processing notification request (tenantId: {}, targets: {})", tenantId, request.getTargets());
		request.setStatus(NotificationRequestStatus.PROCESSING);
		request = notificationRequestService.saveNotificationRequest(request);

		NotificationContext ctx = NotificationContext.builder()
			.tenantId(tenantId)
			.request(request)
			.deliveryTypes(deliveryMethods)
			.template(notificationTemplate)
			.settings(settings)
			.systemSettings(systemSettings)
			.build();

		processNotificationRequestAsync(ctx, targets, callback);
		return request;
	}

	private void forwardToNotificationSchedulerService(String tenantId, Long notificationRequestId) {
//		TransportProtos.NotificationSchedulerServiceMsg.Builder msg = TransportProtos.NotificationSchedulerServiceMsg.newBuilder()
//			.setTenantIdMSB(tenantId.getId().getMostSignificantBits())
//			.setTenantIdLSB(tenantId.getId().getLeastSignificantBits())
//			.setRequestIdMSB(notificationRequestId.getId().getMostSignificantBits())
//			.setRequestIdLSB(notificationRequestId.getId().getLeastSignificantBits())
//			.setTs(System.currentTimeMillis());
//		TransportProtos.ToCoreMsg toCoreMsg = TransportProtos.ToCoreMsg.newBuilder()
//			.setNotificationSchedulerServiceMsg(msg)
//			.build();
//		clusterService.pushMsgToCore(tenantId, notificationRequestId, toCoreMsg, null);
	}


	private void processNotificationRequestAsync(NotificationContext ctx, List<NotificationTarget> targets, FutureCallback<NotificationRequestStats> callback) {
		notificationExecutor.submit(() -> {
			long startTs = System.currentTimeMillis();
			Long requestId = ctx.getRequest().getId();
			for (NotificationTarget target : targets) {
				try {
					processForTarget(target, ctx);
				} catch (Exception e) {
					log.error("[{}] Failed to process notification request for target {}", requestId, target.getId(), e);
					ctx.getStats().setError(e.getMessage());
					updateRequestStats(ctx, requestId, ctx.getStats());

					if (callback != null) {
						callback.onFailure(e);
					}
					return;
				}
			}

			NotificationRequestStats stats = ctx.getStats();
			long time = System.currentTimeMillis() - startTs;
			int sent = stats.getTotalSent().get();
			int errors = stats.getTotalErrors().get();
			if (errors > 0) {
				log.info("[{}][{}] Notification request processing finished in {} ms (sent: {}, errors: {})", ctx.getTenantId(), requestId, time, sent, errors);
			} else {
				log.info("[{}][{}] Notification request processing finished in {} ms (sent: {})", ctx.getTenantId(), requestId, time, sent);
			}
			updateRequestStats(ctx, requestId, stats);
			if (callback != null) {
				callback.onSuccess(stats);
			}
		});
	}

	private void processForTarget(NotificationTarget target, NotificationContext ctx) {
		Iterable<? extends NotificationRecipient> recipients;
		switch (target.getConfig().getType()) {
			case PLATFORM_USER: {
				PlatformUserNotificationTargetConfig targetConfig = (PlatformUserNotificationTargetConfig) target.getConfig();
				User user = new User();
				user.setId(1L);
				user.setName("test");
				user.setEmail("zhijun.chen@dmall.com");
				recipients = Arrays.asList(user);
				break;
			}
			default: {
				recipients = Collections.emptyList();
			}
		}

		Set<NotificationDeliveryMethod> deliveryTypes = new HashSet<>(ctx.getDeliveryTypes());
		deliveryTypes.removeIf(deliveryType -> !target.getConfig().getType().getSupportedDeliveryTypes().contains(deliveryType));
		log.debug("[{}] Processing notification request for {} target ({}) for delivery methods {}", ctx.getRequest().getId(), target.getConfig().getType(), target.getId(), deliveryTypes);
		if (deliveryTypes.isEmpty()) {
			return;
		}

		for (NotificationRecipient recipient : recipients) {
			for (NotificationDeliveryMethod deliveryType : deliveryTypes) {
				try {
					processForRecipient(deliveryType, recipient, ctx);
					ctx.getStats().reportSent(deliveryType, recipient);
				} catch (Exception error) {
					ctx.getStats().reportError(deliveryType, error, recipient);
				}
			}
		}
	}

	private void processForRecipient(NotificationDeliveryMethod deliveryType, NotificationRecipient recipient, NotificationContext ctx) throws Exception {
		if (ctx.getStats().contains(deliveryType, recipient.getId())) {
			throw new AlreadySentException();
		} else {
			ctx.getStats().reportProcessed(deliveryType, recipient.getId());
		}

		if (recipient instanceof User) {
//			UserNotificationSettings settings = notificationSettingsService.getUserNotificationSettings(ctx.getTenantId(), ((User) recipient).getId(), false);
//			if (!settings.isEnabled(ctx.getNotificationType(), deliveryMethod)) {
//				throw new RuntimeException("User disabled " + deliveryMethod.getName() + " notifications of this type");
//			}
		}

		NotificationChannel notificationChannel = channels.get(deliveryType);
		NotificationDeliveryTemplate processedTemplate = ctx.getProcessedTemplate(deliveryType, recipient);

		log.trace("[{}] Sending {} notification for recipient {}", ctx.getRequest().getId(), deliveryType, recipient);
		notificationChannel.sendNotification(recipient, processedTemplate, ctx);
	}


	private void updateRequestStats(NotificationContext ctx, Long requestId, NotificationRequestStats stats) {
		try {
//			notificationRequestService.updateNotificationRequest(ctx.getTenantId(), requestId, NotificationRequestStatus.SENT, stats);
		} catch (Exception e) {
			log.error("[{}] Failed to update stats for notification request", requestId, e);
		}
	}

	@Override
	public void sendNotification(User recipient, WebNotificationDeliveryTemplate processedTemplate, NotificationContext ctx) throws Exception {
		NotificationRequest request = ctx.getRequest();
		Notification notification = Notification.builder()
			.requestId(request.getId())
			.recipientId(recipient.getId())
			.type(ctx.getNotificationType())
			.deliveryMethod(NotificationDeliveryMethod.WEB)
			.subject(processedTemplate.getSubject())
			.text(processedTemplate.getBody())
			.config(processedTemplate.getConfig())
			.info(request.getInfo())
			.status(NotificationStatus.SENT)
			.build();

		log.info("notification:{}", notification);
		try {
//			notification = notificationService.saveNotification(recipient.getTenantId(), notification);
		} catch (Exception e) {
			log.error("Failed to create notification for recipient {}", recipient.getId(), e);
			throw e;
		}

//		NotificationUpdate update = NotificationUpdate.builder()
//			.created(true)
//			.notification(notification)
//			.build();
//		onNotificationUpdate(recipient.getTenantId(), recipient.getId(), update);
	}

	@Override
	public Set<NotificationDeliveryMethod> getAvailableDeliveryTypes(String tenantId) {
		return channels.values().stream()
			.filter(channel -> {
				try {
					channel.check(tenantId);
					return true;
				} catch (Exception e) {
					return false;
				}
			})
			.map(NotificationChannel::getDeliveryMethod)
			.collect(Collectors.toSet());
	}

	@Override
	public void deleteNotificationRequest(Long notificationRequestId) {

	}

	@Override
	public void check(String tenantId) throws Exception {

	}


	@Override
	public NotificationDeliveryMethod getDeliveryMethod() {
		return NotificationDeliveryMethod.WEB;
	}
}
