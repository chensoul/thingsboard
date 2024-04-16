package com.chensoul.netty.thingsboard.mqtt; /**
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

import com.chensoul.netty.thingsboard.mqtt.session.MqttDeviceAwareSessionContext;
import com.chensoul.netty.thingsboard.mqtt.topic.MqttTopicFilter;
import com.chensoul.netty.thingsboard.mqtt.topic.MqttTopicFilterFactory;
import com.chensoul.netty.thingsboard.mqtt.topic.MqttTopicMatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.util.ReferenceCountUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.domain.iot.deviceprofile.DeviceProfile;
import org.thingsboard.transport.TransportPayloadType;

/**
 * @author Andrew Shvayka
 */
@Slf4j
public class DeviceSessionCtx extends MqttDeviceAwareSessionContext {

	@Getter
	@Setter
	private ChannelHandlerContext channel;

	@Getter
	private final MqttTransportContext context;

	private final AtomicInteger msgIdSeq = new AtomicInteger(0);

	private final ConcurrentLinkedQueue<MqttMessage> msgQueue = new ConcurrentLinkedQueue<>();

	@Getter
	private final Lock msgQueueProcessorLock = new ReentrantLock();

	private final AtomicInteger msgQueueSize = new AtomicInteger(0);

	@Getter
	@Setter
	private MqttVersion mqttVersion;

	private volatile MqttTopicFilter telemetryTopicFilter = MqttTopicFilterFactory.getDefaultTelemetryFilter();
	private volatile MqttTopicFilter attributesPublishTopicFilter = MqttTopicFilterFactory.getDefaultAttributesFilter();
	private volatile MqttTopicFilter attributesSubscribeTopicFilter = MqttTopicFilterFactory.getDefaultAttributesFilter();
	private volatile TransportPayloadType payloadType = TransportPayloadType.JSON;
	private volatile boolean jsonPayloadFormatCompatibilityEnabled;
	private volatile boolean useJsonPayloadFormatForDefaultDownlinkTopics;
	private volatile boolean sendAckOnValidationException;

	@Getter
	private volatile boolean deviceProfileMqttTransportType;

	public DeviceSessionCtx(UUID sessionId, ConcurrentMap<MqttTopicMatcher, Integer> mqttQoSMap, MqttTransportContext context) {
		super(sessionId, mqttQoSMap, context.getAuthService());
		this.context = context;
	}

	public int nextMsgId() {
		return msgIdSeq.incrementAndGet();
	}

	public boolean isDeviceTelemetryTopic(String topicName) {
		return telemetryTopicFilter.filter(topicName);
	}

	public boolean isJsonPayloadType() {
		return payloadType.equals(TransportPayloadType.JSON);
	}

	public boolean isSendAckOnValidationException() {
		return sendAckOnValidationException;
	}

	public boolean isDeviceAttributesTopic(String topicName) {
		return attributesPublishTopicFilter.filter(topicName);
	}

	public boolean isDeviceSubscriptionAttributesTopic(String topicName) {
		return attributesSubscribeTopicFilter.filter(topicName);
	}

	@Override
	public void setDeviceProfile(DeviceProfile deviceProfile) {
		super.setDeviceProfile(deviceProfile);
//		updateDeviceSessionConfiguration(deviceProfile);
	}

//	@Override
//	public void onDeviceProfileUpdate(TransportProtos.SessionInfoProto sessionInfo, DeviceProfile deviceProfile) {
//		super.onDeviceProfileUpdate(sessionInfo, deviceProfile);
//		updateDeviceSessionConfiguration(deviceProfile);
//	}
//
//	private void updateDeviceSessionConfiguration(DeviceProfile deviceProfile) {
//		DeviceProfileTransportConfiguration transportConfiguration = deviceProfile.getProfileData().getTransportConfiguration();
//		if (transportConfiguration.getType().equals(DeviceTransportType.MQTT) &&
//			transportConfiguration instanceof MqttDeviceProfileTransportConfiguration) {
//			MqttDeviceProfileTransportConfiguration mqttConfig = (MqttDeviceProfileTransportConfiguration) transportConfiguration;
//			TransportPayloadTypeConfiguration transportPayloadTypeConfiguration = mqttConfig.getTransportPayloadTypeConfiguration();
//			payloadType = transportPayloadTypeConfiguration.getTransportPayloadType();
//			deviceProfileMqttTransportType = true;
//			telemetryTopicFilter = MqttTopicFilterFactory.toFilter(mqttConfig.getDeviceTelemetryTopic());
//			attributesPublishTopicFilter = MqttTopicFilterFactory.toFilter(mqttConfig.getDeviceAttributesTopic());
//			attributesSubscribeTopicFilter = MqttTopicFilterFactory.toFilter(mqttConfig.getDeviceAttributesSubscribeTopic());
//			sendAckOnValidationException = mqttConfig.isSendAckOnValidationException();
//			if (TransportPayloadType.PROTOBUF.equals(payloadType)) {
//				ProtoTransportPayloadConfiguration protoTransportPayloadConfig = (ProtoTransportPayloadConfiguration) transportPayloadTypeConfiguration;
//				updateDynamicMessageDescriptors(protoTransportPayloadConfig);
//				jsonPayloadFormatCompatibilityEnabled = protoTransportPayloadConfig.isEnableCompatibilityWithJsonPayloadFormat();
//				useJsonPayloadFormatForDefaultDownlinkTopics = jsonPayloadFormatCompatibilityEnabled && protoTransportPayloadConfig.isUseJsonPayloadFormatForDefaultDownlinkTopics();
//			}
//		} else {
//			telemetryTopicFilter = MqttTopicFilterFactory.getDefaultTelemetryFilter();
//			attributesPublishTopicFilter = MqttTopicFilterFactory.getDefaultAttributesFilter();
//			payloadType = TransportPayloadType.JSON;
//			deviceProfileMqttTransportType = false;
//			sendAckOnValidationException = false;
//		}
//		updateAdaptor();
//	}
//
//	private void updateDynamicMessageDescriptors(ProtoTransportPayloadConfiguration protoTransportPayloadConfig) {
//		telemetryDynamicMessageDescriptor = protoTransportPayloadConfig.getTelemetryDynamicMessageDescriptor(protoTransportPayloadConfig.getDeviceTelemetryProtoSchema());
//		attributesDynamicMessageDescriptor = protoTransportPayloadConfig.getAttributesDynamicMessageDescriptor(protoTransportPayloadConfig.getDeviceAttributesProtoSchema());
//		rpcResponseDynamicMessageDescriptor = protoTransportPayloadConfig.getRpcResponseDynamicMessageDescriptor(protoTransportPayloadConfig.getDeviceRpcResponseProtoSchema());
//		rpcRequestDynamicMessageBuilder = protoTransportPayloadConfig.getRpcRequestDynamicMessageBuilder(protoTransportPayloadConfig.getDeviceRpcRequestProtoSchema());
//	}

	public void addToQueue(MqttMessage msg) {
		msgQueueSize.incrementAndGet();
		ReferenceCountUtil.retain(msg);
		msgQueue.add(msg);
	}

	public void tryProcessQueuedMsgs(Consumer<MqttMessage> msgProcessor) {
		while (!msgQueue.isEmpty()) {
			if (msgQueueProcessorLock.tryLock()) {
				try {
					MqttMessage msg;
					while ((msg = msgQueue.poll()) != null) {
						try {
							msgQueueSize.decrementAndGet();
							msgProcessor.accept(msg);
						} finally {
							ReferenceCountUtil.safeRelease(msg);
						}
					}
				} finally {
					msgQueueProcessorLock.unlock();
				}
			} else {
				return;
			}
		}
	}

	public int getMsgQueueSize() {
		return msgQueueSize.get();
	}

	public void release() {
		if (!msgQueue.isEmpty()) {
			log.warn("doDisconnect for device {} but unprocessed messages {} left in the msg queue", getDeviceId(), msgQueue.size());
			msgQueue.forEach(ReferenceCountUtil::safeRelease);
			msgQueue.clear();
		}
	}

	public Collection<MqttMessage> getMsgQueueSnapshot() {
		return Collections.unmodifiableCollection(msgQueue);
	}

}
