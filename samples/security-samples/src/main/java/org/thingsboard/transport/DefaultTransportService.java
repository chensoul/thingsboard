package org.thingsboard.transport;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thingsboard.common.anntation.AfterStartUp;
import org.thingsboard.common.stats.MessagesStats;
import org.thingsboard.common.stats.StatsFactory;
import org.thingsboard.common.stats.StatsType;
import org.thingsboard.common.util.ThingsBoardExecutors;
import org.thingsboard.common.util.ThingsBoardThreadFactory;
import org.thingsboard.domain.iot.deviceprofile.DeviceProfile;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;
import org.thingsboard.domain.message.GetDeviceCredentialsRequestMsg;
import org.thingsboard.domain.message.GetDeviceCredentialsResponseMsg;
import org.thingsboard.domain.message.GetDeviceRequestMsg;
import org.thingsboard.domain.message.GetDeviceResponseMsg;
import org.thingsboard.domain.message.TbProtoQueueMsg;
import org.thingsboard.domain.message.ToTransportMsg;
import org.thingsboard.domain.message.TransportApiRequestMsg;
import org.thingsboard.domain.message.TransportApiResponseMsg;
import org.thingsboard.domain.message.ValidateBasicMqttCredRequestMsg;
import org.thingsboard.domain.message.ValidateDeviceCredentialsResponse;
import org.thingsboard.domain.message.ValidateDeviceCredentialsResponseMsg;
import org.thingsboard.domain.message.ValidateDeviceTokenRequestMsg;
import org.thingsboard.queue.common.AsyncCallbackTemplate;
import org.thingsboard.queue.spi.TbQueueConsumer;
import org.thingsboard.queue.spi.TbQueueRequestTemplate;
import org.thingsboard.transport.auth.TransportDeviceInfo;
import org.thingsboard.transport.session.SessionMetaData;
import org.thingsboard.transport.session.SessionMsgListener;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@Component
public class DefaultTransportService implements TransportService {
	protected TbQueueRequestTemplate<TbProtoQueueMsg<TransportApiRequestMsg>, TbProtoQueueMsg<TransportApiResponseMsg>> transportApiRequestTemplate;

	@Value("${queue.transport.poll_interval}")
	private int notificationsPollDuration;

	public final ConcurrentMap<String, SessionMetaData> sessions = new ConcurrentHashMap<>();

	protected TbQueueConsumer<TbProtoQueueMsg<ToTransportMsg>> transportNotificationsConsumer;

	protected MessagesStats transportApiStats;

	private StatsFactory statsFactory;
	private TbTransportQueueFactory queueProvider;

	private ExecutorService transportCallbackExecutor;
	private ExecutorService mainConsumerExecutor;

	private volatile boolean stopped = false;

	public DefaultTransportService(StatsFactory statsFactory, TbTransportQueueFactory queueProvider) {
		this.statsFactory = statsFactory;
		this.queueProvider = queueProvider;
	}

	@PostConstruct
	public void init() {
		this.transportApiStats = statsFactory.createMessagesStats(StatsType.TRANSPORT.getName() + ".producer");

		this.transportCallbackExecutor = ThingsBoardExecutors.newWorkStealingPool(20, getClass());
		transportApiRequestTemplate = queueProvider.createTransportApiRequestTemplate();
		transportApiRequestTemplate.setMessagesStats(transportApiStats);
		transportApiRequestTemplate.init();

		transportNotificationsConsumer = queueProvider.createTransportNotificationsConsumer();
//		TopicPartitionInfo tpi = topicService.getNotificationsTopic(ServiceType.TB_TRANSPORT, serviceInfoProvider.getServiceId());
//		transportNotificationsConsumer.subscribe(Collections.singleton(tpi));

		mainConsumerExecutor = Executors.newSingleThreadExecutor(ThingsBoardThreadFactory.forName("transport-consumer"));
	}

	@AfterStartUp(order = AfterStartUp.TRANSPORT_SERVICE)
	public void start() {
		mainConsumerExecutor.execute(() -> {
			while (!stopped) {
				try {
					List<TbProtoQueueMsg<ToTransportMsg>> records = transportNotificationsConsumer.poll(notificationsPollDuration);
					if (records.size() == 0) {
						continue;
					}
					records.forEach(record -> {
						try {
							processToTransportMsg(record.getValue());
						} catch (Throwable e) {
							log.warn("Failed to process the notification.", e);
						}
					});
					transportNotificationsConsumer.commit();
				} catch (Exception e) {
					if (!stopped) {
						log.warn("Failed to obtain messages from queue.", e);
						try {
							Thread.sleep(notificationsPollDuration);
						} catch (InterruptedException e2) {
							log.trace("Failed to wait until the server has capacity to handle new requests", e2);
						}
					}
				}
			}
		});
	}

	protected void processToTransportMsg(ToTransportMsg toSessionMsg) {
		String sessionId = toSessionMsg.getSessionId();
		SessionMetaData md = sessions.get(sessionId);
		if (md != null) {
			log.trace("[{}] Processing notification: {}", sessionId, toSessionMsg);
			SessionMsgListener listener = md.getListener();
			transportCallbackExecutor.submit(() -> {

			});
//			if (md.getSessionType() == TransportProtos.SessionType.SYNC) {
//				deregisterSession(md.getSessionInfo());
//			}
		} else {
			log.trace("Processing broadcast notification: {}", toSessionMsg);

		}
	}

	@Override
	public GetDeviceResponseMsg getDevice(GetDeviceRequestMsg requestMsg) {
		return null;
	}

	@Override
	public GetDeviceCredentialsResponseMsg getDeviceCredentials(GetDeviceCredentialsRequestMsg requestMsg) {
		return null;
	}

	@Override
	public void process(DeviceTransportType transportType, ValidateDeviceTokenRequestMsg msg, TransportServiceCallback<ValidateDeviceCredentialsResponse> callback) {
		TbProtoQueueMsg<TransportApiRequestMsg> protoMsg = new TbProtoQueueMsg<>();
		doProcess(transportType, protoMsg, callback);
	}

	@Override
	public void process(DeviceTransportType transportType, ValidateBasicMqttCredRequestMsg msg, TransportServiceCallback<ValidateDeviceCredentialsResponse> callback) {
		TbProtoQueueMsg<TransportApiRequestMsg> protoMsg = new TbProtoQueueMsg<>();
		doProcess(transportType, protoMsg, callback);
	}

	private void doProcess(DeviceTransportType transportType, TbProtoQueueMsg<TransportApiRequestMsg> protoMsg,
						   TransportServiceCallback<ValidateDeviceCredentialsResponse> callback) {
		ListenableFuture<ValidateDeviceCredentialsResponse> response = Futures.transform(transportApiRequestTemplate.send(protoMsg), tmp -> {
			ValidateDeviceCredentialsResponseMsg msg = tmp.getValue().getValidateCredResponseMsg();
			ValidateDeviceCredentialsResponse.ValidateDeviceCredentialsResponseBuilder result = ValidateDeviceCredentialsResponse.builder();
			if (msg.hasDeviceInfo()) {
				result.credential(msg.getCredential());
				TransportDeviceInfo tdi = msg.getDeviceInfo();
				result.deviceInfo(tdi);
				if (msg.hasDeviceProfile()) {
					DeviceProfile profile = msg.getDeviceProfile();
					if (transportType != DeviceTransportType.DEFAULT
						&& profile != null && profile.getTransportType() != DeviceTransportType.DEFAULT && profile.getTransportType() != transportType) {
						log.debug("[{}] Device profile [{}] has different transport type: {}, expected: {}", tdi.getDeviceId(), tdi.getDeviceProfileId(), profile.getTransportType(), transportType);
						throw new IllegalStateException("Device profile has different transport type: " + profile.getTransportType() + ". Expected: " + transportType);
					}
					result.deviceProfile(profile);
				}
			}
			return result.build();
		}, MoreExecutors.directExecutor());
		AsyncCallbackTemplate.withCallback(response, callback::onSuccess, callback::onError, transportCallbackExecutor);
	}
}
