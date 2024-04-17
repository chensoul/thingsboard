package org.thingsboard.transport.queue;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thingsboard.common.util.EncryptionUtil;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.util.ThingsBoardExecutors;
import org.thingsboard.domain.iot.device.Device;
import org.thingsboard.domain.iot.device.DeviceCredential;
import org.thingsboard.domain.iot.device.DeviceCredentialService;
import org.thingsboard.domain.iot.device.model.DeviceCredentialType;
import org.thingsboard.domain.iot.device.DeviceService;
import org.thingsboard.domain.message.ProtoQueueMsg;
import org.thingsboard.domain.message.TransportApiRequestMsg;
import org.thingsboard.domain.message.TransportApiResponseMsg;
import org.thingsboard.domain.message.ValidateBasicMqttCredRequestMsg;
import org.thingsboard.domain.message.ValidateDeviceCredentialsResponseMsg;
import org.thingsboard.domain.message.ValidateDeviceTokenRequestMsg;
import org.thingsboard.transport.BasicCredentialsValidationResult;
import static org.thingsboard.transport.BasicCredentialsValidationResult.PASSWORD_MISMATCH;
import static org.thingsboard.transport.BasicCredentialsValidationResult.VALID;
import org.thingsboard.transport.auth.BasicMqttCredentials;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class DefaultTransportQueueHandler implements TransportQueueHandler {
	private final DeviceCredentialService deviceCredentialService;
	private final DeviceService deviceService;

	@Value("${queue.transport_api.max_core_handler_threads:16}")
	private int maxCoreHandlerThreads;
	ListeningExecutorService handlerExecutor;

	@PostConstruct
	public void init() {
		handlerExecutor = MoreExecutors.listeningDecorator(ThingsBoardExecutors.newWorkStealingPool(maxCoreHandlerThreads, "transport-api-service-core-handler"));
	}

	@PreDestroy
	public void destroy() {
		if (handlerExecutor != null) {
			handlerExecutor.shutdownNow();
		}
	}

	@Override
	public ListenableFuture<ProtoQueueMsg<TransportApiResponseMsg>> handle(ProtoQueueMsg<TransportApiRequestMsg> tbProtoQueueMsg) {
		TransportApiRequestMsg transportApiRequestMsg = tbProtoQueueMsg.getValue();
		ListenableFuture<TransportApiResponseMsg> result = null;

		if (transportApiRequestMsg.getValidateTokenRequestMsg() != null) {
			ValidateDeviceTokenRequestMsg msg = transportApiRequestMsg.getValidateTokenRequestMsg();
			final String token = msg.getToken();
			result = handlerExecutor.submit(() -> validateCredential(token, DeviceCredentialType.ACCESS_TOKEN));
		} else if (transportApiRequestMsg.getBasicMqttCredRequestMsg() != null) {
			ValidateBasicMqttCredRequestMsg msg = transportApiRequestMsg.getBasicMqttCredRequestMsg();
			result = handlerExecutor.submit(() -> validateCredential(msg));
		}
		return null;
	}

	private TransportApiResponseMsg validateCredential(ValidateBasicMqttCredRequestMsg mqtt) {
		DeviceCredential credentials;
		if (StringUtils.isEmpty(mqtt.getUserName())) {
			credentials = checkMqttCredentials(mqtt, EncryptionUtil.getSha3Hash(mqtt.getClientId()));
			if (credentials != null) {
				return getDeviceInfo(credentials);
			} else {
				return getEmptyTransportApiResponse();
			}
		} else {
			credentials = deviceCredentialService.findDeviceCredentialsByCredentialsId(
				EncryptionUtil.getSha3Hash("|", mqtt.getClientId(), mqtt.getUserName()));
			if (checkIsMqttCredentials(credentials)) {
				var validationResult = validateMqttCredentials(mqtt, credentials);
				if (VALID.equals(validationResult)) {
					return getDeviceInfo(credentials);
				} else if (PASSWORD_MISMATCH.equals(validationResult)) {
					return getEmptyTransportApiResponse();
				} else {
					return validateUserNameCredentials(mqtt);
				}
			} else {
				return validateUserNameCredentials(mqtt);
			}
		}
	}

	private static boolean checkIsMqttCredentials(DeviceCredential credentials) {
		return credentials != null && DeviceCredentialType.MQTT_BASIC.equals(credentials.getCredentialType());
	}


	private TransportApiResponseMsg validateUserNameCredentials(ValidateBasicMqttCredRequestMsg mqtt) {
		DeviceCredential credentials = deviceCredentialService.findDeviceCredentialsByCredentialsId(mqtt.getUserName());
		if (credentials != null) {
			switch (credentials.getCredentialType()) {
				case ACCESS_TOKEN:
					return getDeviceInfo(credentials);
				case MQTT_BASIC:
					if (VALID.equals(validateMqttCredentials(mqtt, credentials))) {
						return getDeviceInfo(credentials);
					} else {
						return getEmptyTransportApiResponse();
					}
			}
		}
		return getEmptyTransportApiResponse();
	}

	private DeviceCredential checkMqttCredentials(ValidateBasicMqttCredRequestMsg clientCred, String credId) {
		return checkMqttCredentials(clientCred, deviceCredentialService.findDeviceCredentialsByCredentialsId(credId));
	}

	private DeviceCredential checkMqttCredentials(ValidateBasicMqttCredRequestMsg clientCred, DeviceCredential deviceCredentials) {
		if (deviceCredentials != null && deviceCredentials.getCredentialType() == DeviceCredentialType.MQTT_BASIC) {
			if (VALID.equals(validateMqttCredentials(clientCred, deviceCredentials))) {
				return deviceCredentials;
			}
		}
		return null;
	}

	private BasicCredentialsValidationResult validateMqttCredentials(ValidateBasicMqttCredRequestMsg clientCred, DeviceCredential deviceCredentials) {
		BasicMqttCredentials dbCred = JacksonUtil.fromString(deviceCredentials.getCredentialValue(), BasicMqttCredentials.class);
		if (!StringUtils.isEmpty(dbCred.getClientId()) && !dbCred.getClientId().equals(clientCred.getClientId())) {
			return BasicCredentialsValidationResult.HASH_MISMATCH;
		}
		if (!StringUtils.isEmpty(dbCred.getUserName()) && !dbCred.getUserName().equals(clientCred.getUserName())) {
			return BasicCredentialsValidationResult.HASH_MISMATCH;
		}
		if (!StringUtils.isEmpty(dbCred.getPassword())) {
			if (StringUtils.isEmpty(clientCred.getPassword())) {
				return PASSWORD_MISMATCH;
			} else {
				return dbCred.getPassword().equals(clientCred.getPassword()) ? VALID : PASSWORD_MISMATCH;
			}
		}
		return VALID;
	}


	private TransportApiResponseMsg validateCredential(String credentialsId, DeviceCredentialType credentialsType) {
		DeviceCredential credentials = deviceCredentialService.findDeviceCredentialsByCredentialsId(credentialsId);
		if (credentials != null && credentials.getCredentialType() == credentialsType) {
			return getDeviceInfo(credentials);
		} else {
			return getEmptyTransportApiResponse();
		}
	}

	TransportApiResponseMsg getDeviceInfo(DeviceCredential credentials) {
		Device device = deviceService.findDeviceById(credentials.getDeviceId());
		if (device == null) {
			log.trace("[{}] Failed to lookup device by id", credentials.getDeviceId());
			return getEmptyTransportApiResponse();
		}
		ValidateDeviceCredentialsResponseMsg.ValidateDeviceCredentialsResponseMsgBuilder builder = ValidateDeviceCredentialsResponseMsg.builder();
//			builder.deviceInfo(ProtoUtils.toDeviceInfoProto(device));
//			DeviceProfile deviceProfile = deviceProfileCache.get(device.getTenantId(), device.getDeviceProfileId());
//			if (deviceProfile != null) {
//				builder.deviceProfile(ProtoUtils.toProto(deviceProfile));
//			} else {
//				log.warn("[{}] Failed to find device profile [{}] for device. ", device.getId(), device.getDeviceProfileId());
//			}
		if (!StringUtils.isEmpty(credentials.getCredentialValue())) {
			builder.credential(credentials.getCredentialValue());
		}
		return TransportApiResponseMsg.builder()
			.validateCredResponseMsg(builder.build()).build();
	}

	private TransportApiResponseMsg getEmptyTransportApiResponse() {
		return TransportApiResponseMsg.builder()
			.validateCredResponseMsg(ValidateDeviceCredentialsResponseMsg.builder().build()).build();
	}

}
