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
package com.chensoul.netty.thingsboard.mqtt;

import static com.chensoul.netty.thingsboard.mqtt.MqttTransportServerInitializer.ADDRESS;
import com.chensoul.netty.thingsboard.mqtt.topic.MqttTopicMatcher;
import com.chensoul.netty.thingsboard.mqtt.topic.MqttTopics;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import static io.netty.handler.codec.mqtt.MqttMessageType.PINGRESP;
import static io.netty.handler.codec.mqtt.MqttMessageType.SUBACK;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_LEAST_ONCE;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.net.ssl.SSLPeerUnverifiedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.thingsboard.common.util.EncryptionUtil;
import org.thingsboard.common.util.SslUtil;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;
import org.thingsboard.domain.message.ValidateBasicMqttCredRequestMsg;
import org.thingsboard.domain.message.ValidateDeviceCredentialsResponse;
import org.thingsboard.transport.TransportService;
import org.thingsboard.transport.TransportServiceCallback;
import org.thingsboard.transport.auth.TransportDeviceInfo;

/**
 * @author Andrew Shvayka
 */
@Slf4j
public class MqttTransportHandler extends ChannelInboundHandlerAdapter implements GenericFutureListener<Future<? super Void>> {
//	private static final Pattern FW_REQUEST_PATTERN = Pattern.compile(MqttTopics.DEVICE_FIRMWARE_REQUEST_TOPIC_PATTERN);
//	private static final Pattern SW_REQUEST_PATTERN = Pattern.compile(MqttTopics.DEVICE_SOFTWARE_REQUEST_TOPIC_PATTERN);

	private static final String PAYLOAD_TOO_LARGE = "PAYLOAD_TOO_LARGE";
	private static final MqttQoS MAX_SUPPORTED_QOS_LVL = AT_LEAST_ONCE;

	private final UUID sessionId;

	protected final MqttTransportContext context;
	private final TransportService transportService;
	private final SslHandler sslHandler;
	private final ConcurrentMap<MqttTopicMatcher, Integer> mqttQoSMap;

	final DeviceSessionCtx deviceSessionCtx;
	volatile InetSocketAddress address;

	private TopicType attrSubTopicType;
	private TopicType rpcSubTopicType;
	private TopicType attrReqTopicType;
	private TopicType toServerRpcSubTopicType;

	MqttTransportHandler(MqttTransportContext context, SslHandler sslHandler) {
		this.sessionId = UUID.randomUUID();
		this.context = context;
		this.transportService = context.getTransportService();
		this.sslHandler = sslHandler;
		this.mqttQoSMap = new ConcurrentHashMap<>();
		this.deviceSessionCtx = new DeviceSessionCtx(sessionId, mqttQoSMap, context);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		context.channelRegistered();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.info("[{}] Processing msg: {}", sessionId, msg);

		if (address == null) {
			address = getAddress(ctx);
		}

		try {
			if (msg instanceof MqttMessage) {
				MqttMessage message = (MqttMessage) msg;
				if (message.decoderResult().isSuccess()) {
					processMqttMsg(ctx, message);
				} else {
					log.error("[{}] Message decoding failed: {}", sessionId, message.decoderResult().cause().getMessage());
					closeCtx(ctx);
				}
			} else {
				log.debug("[{}] Received non mqtt message: {}", sessionId, msg.getClass().getSimpleName());
				closeCtx(ctx);
			}
		} finally {
			ReferenceCountUtil.safeRelease(msg);
		}
	}

	void processMqttMsg(ChannelHandlerContext ctx, MqttMessage msg) {
		if (msg.fixedHeader() == null) {
			log.info("[{}:{}] Invalid message received", address.getHostName(), address.getPort());
			closeCtx(ctx);
			return;
		}
		deviceSessionCtx.setChannel(ctx);
		if (MqttMessageType.CONNECT.equals(msg.fixedHeader().messageType())) {
			processConnect(ctx, (MqttConnectMessage) msg);
		} else {
			enqueueRegularSessionMsg(ctx, msg);
		}
	}

	void enqueueRegularSessionMsg(ChannelHandlerContext ctx, MqttMessage msg) {
		final int queueSize = deviceSessionCtx.getMsgQueueSize();
		if (queueSize >= context.getMessageQueueSizePerDeviceLimit()) {
			log.info("Closing current session because msq queue size for device {} exceed limit {} with msgQueueSize counter {} and actual queue size {}",
				deviceSessionCtx.getDeviceId(), context.getMessageQueueSizePerDeviceLimit(), queueSize, deviceSessionCtx.getMsgQueueSize());
			closeCtx(ctx);
			return;
		}

		deviceSessionCtx.addToQueue(msg);
		processMsgQueue(ctx); //Under the normal conditions the msg queue will contain 0 messages. Many messages will be processed on device connect event in separate thread pool
	}

	void processMsgQueue(ChannelHandlerContext ctx) {
		if (!deviceSessionCtx.isConnected()) {
			log.trace("[{}][{}] Postpone processing msg due to device is not connected. Msg queue size is {}", sessionId, deviceSessionCtx.getDeviceId(), deviceSessionCtx.getMsgQueueSize());
			return;
		}
		deviceSessionCtx.tryProcessQueuedMsgs(msg -> processRegularSessionMsg(ctx, msg));
	}

	void processRegularSessionMsg(ChannelHandlerContext ctx, MqttMessage msg) {
		switch (msg.fixedHeader().messageType()) {
			case PUBLISH:
				processPublish(ctx, (MqttPublishMessage) msg);
				break;
			case SUBSCRIBE:
				processSubscribe(ctx, (MqttSubscribeMessage) msg);
				break;
			case UNSUBSCRIBE:
				processUnsubscribe(ctx, (MqttUnsubscribeMessage) msg);
				break;
			case PINGREQ:
				if (checkConnected(ctx, msg)) {
					ctx.writeAndFlush(new MqttMessage(new MqttFixedHeader(PINGRESP, false, AT_MOST_ONCE, false, 0)));
//					transportService.recordActivity(deviceSessionCtx.getSessionInfo());
				}
				break;
			case DISCONNECT:
				closeCtx(ctx);
				break;
			case PUBACK:
				int msgId = ((MqttPubAckMessage) msg).variableHeader().messageId();
//				TransportProtos.ToDeviceRpcRequestMsg rpcRequest = rpcAwaitingAck.remove(msgId);
//				if (rpcRequest != null) {
//					transportService.process(deviceSessionCtx.getSessionInfo(), rpcRequest, RpcStatus.DELIVERED, true, TransportServiceCallback.EMPTY);
//				}
				break;
			default:
				break;
		}
	}

	private void processUnsubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage mqttMsg) {
		if (!checkConnected(ctx, mqttMsg)) {
			ctx.writeAndFlush(createUnSubAckMessage(mqttMsg.variableHeader().messageId(), Collections.singletonList(ReturnCode.NOT_AUTHORIZED_5.shortValue())));
			return;
		}
		boolean activityReported = false;
		List<Short> unSubResults = new ArrayList<>();
		log.trace("[{}] Processing subscription [{}]!", sessionId, mqttMsg.variableHeader().messageId());
		for (String topicName : mqttMsg.payload().topics()) {
			MqttTopicMatcher matcher = new MqttTopicMatcher(topicName);
			if (mqttQoSMap.containsKey(matcher)) {
				mqttQoSMap.remove(matcher);
				try {
					short resultValue = ReturnCode.SUCCESS.shortValue();
					switch (topicName) {
						case MqttTopics.DEVICE_ATTRIBUTES_TOPIC:
						case MqttTopics.DEVICE_ATTRIBUTES_SHORT_TOPIC:
						case MqttTopics.DEVICE_ATTRIBUTES_SHORT_PROTO_TOPIC:
						case MqttTopics.DEVICE_ATTRIBUTES_SHORT_JSON_TOPIC: {
//							transportService.process(deviceSessionCtx.getSessionInfo(),
//								TransportProtos.SubscribeToAttributeUpdatesMsg.newBuilder().setUnsubscribe(true).build(), null);
							activityReported = true;
							break;
						}
						case MqttTopics.DEVICE_RPC_REQUESTS_SUB_TOPIC:
						case MqttTopics.DEVICE_RPC_REQUESTS_SUB_SHORT_TOPIC:
						case MqttTopics.DEVICE_RPC_REQUESTS_SUB_SHORT_JSON_TOPIC:
						case MqttTopics.DEVICE_RPC_REQUESTS_SUB_SHORT_PROTO_TOPIC: {
//							transportService.process(deviceSessionCtx.getSessionInfo(),
//								TransportProtos.SubscribeToRPCMsg.newBuilder().setUnsubscribe(true).build(), null);
							activityReported = true;
							break;
						}
						case MqttTopics.DEVICE_RPC_RESPONSE_SUB_TOPIC:
						case MqttTopics.DEVICE_RPC_RESPONSE_SUB_SHORT_TOPIC:
						case MqttTopics.DEVICE_RPC_RESPONSE_SUB_SHORT_JSON_TOPIC:
						case MqttTopics.DEVICE_RPC_RESPONSE_SUB_SHORT_PROTO_TOPIC:
						case MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_TOPIC:
						case MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_SHORT_TOPIC:
						case MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_SHORT_JSON_TOPIC:
						case MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_SHORT_PROTO_TOPIC:
						case MqttTopics.GATEWAY_ATTRIBUTES_TOPIC:
						case MqttTopics.GATEWAY_RPC_TOPIC:
						case MqttTopics.GATEWAY_ATTRIBUTES_RESPONSE_TOPIC:
						case MqttTopics.DEVICE_PROVISION_RESPONSE_TOPIC:
						case MqttTopics.DEVICE_FIRMWARE_RESPONSES_TOPIC:
						case MqttTopics.DEVICE_FIRMWARE_ERROR_TOPIC:
						case MqttTopics.DEVICE_SOFTWARE_RESPONSES_TOPIC:
						case MqttTopics.DEVICE_SOFTWARE_ERROR_TOPIC: {
							activityReported = true;
							break;
						}
						default:
							log.trace("[{}] Failed to process unsubscription [{}] to [{}]", sessionId, mqttMsg.variableHeader().messageId(), topicName);
							resultValue = ReturnCode.TOPIC_FILTER_INVALID.shortValue();
					}
					unSubResults.add(resultValue);
				} catch (Exception e) {
					log.debug("[{}] Failed to process unsubscription [{}] to [{}]", sessionId, mqttMsg.variableHeader().messageId(), topicName);
					unSubResults.add(ReturnCode.IMPLEMENTATION_SPECIFIC.shortValue());
				}
			} else {
				log.debug("[{}] Failed to process unsubscription [{}] to [{}] - Subscription not found", sessionId, mqttMsg.variableHeader().messageId(), topicName);
				unSubResults.add(ReturnCode.NO_SUBSCRIPTION_EXISTED.shortValue());
			}
		}
		if (!activityReported) {
//			transportService.recordActivity(deviceSessionCtx.getSessionInfo());
		}
		ctx.writeAndFlush(createUnSubAckMessage(mqttMsg.variableHeader().messageId(), unSubResults));
	}

	private MqttMessage createUnSubAckMessage(int msgId, List<Short> resultCodes) {
		MqttMessageBuilders.UnsubAckBuilder unsubAckBuilder = MqttMessageBuilders.unsubAck();
		unsubAckBuilder.packetId(msgId);
		if (MqttVersion.MQTT_5.equals(deviceSessionCtx.getMqttVersion())) {
			unsubAckBuilder.addReasonCodes(resultCodes.toArray(Short[]::new));
		}
		return unsubAckBuilder.build();
	}

	private void processSubscribe(ChannelHandlerContext ctx, MqttSubscribeMessage mqttMsg) {
		if (!checkConnected(ctx, mqttMsg)) {
			int returnCode = ReturnCodeResolver.getSubscriptionReturnCode(deviceSessionCtx.getMqttVersion(), ReturnCode.NOT_AUTHORIZED_5);
			ctx.writeAndFlush(createSubAckMessage(mqttMsg.variableHeader().messageId(), Collections.singletonList(returnCode)));
			return;
		}
		log.trace("[{}] Processing subscription [{}]!", sessionId, mqttMsg.variableHeader().messageId());
		List<Integer> grantedQoSList = new ArrayList<>();
		boolean activityReported = false;
		for (MqttTopicSubscription subscription : mqttMsg.payload().topicSubscriptions()) {
			String topic = subscription.topicName();
			MqttQoS reqQoS = subscription.qualityOfService();
			if (deviceSessionCtx.isDeviceSubscriptionAttributesTopic(topic)) {
				processAttributesSubscribe(grantedQoSList, topic, reqQoS, TopicType.V1);
				activityReported = true;
				continue;
			}
			try {

				switch (topic) {
					case MqttTopics.DEVICE_ATTRIBUTES_SHORT_JSON_TOPIC: {
						processAttributesSubscribe(grantedQoSList, topic, reqQoS, TopicType.V2_JSON);
						activityReported = true;
						break;
					}

					case MqttTopics.DEVICE_RPC_REQUESTS_SUB_TOPIC: {
						processRpcSubscribe(grantedQoSList, topic, reqQoS, TopicType.V1);
						activityReported = true;
						break;
					}
					case MqttTopics.DEVICE_RPC_REQUESTS_SUB_SHORT_TOPIC: {
						processRpcSubscribe(grantedQoSList, topic, reqQoS, TopicType.V2);
						activityReported = true;
						break;
					}
					case MqttTopics.DEVICE_RPC_REQUESTS_SUB_SHORT_JSON_TOPIC: {
						processRpcSubscribe(grantedQoSList, topic, reqQoS, TopicType.V2_JSON);
						activityReported = true;
						break;
					}
					case MqttTopics.DEVICE_RPC_REQUESTS_SUB_SHORT_PROTO_TOPIC: {
						processRpcSubscribe(grantedQoSList, topic, reqQoS, TopicType.V2_PROTO);
						activityReported = true;
						break;
					}
					case MqttTopics.DEVICE_RPC_RESPONSE_SUB_TOPIC:
					case MqttTopics.DEVICE_RPC_RESPONSE_SUB_SHORT_TOPIC:
					case MqttTopics.DEVICE_RPC_RESPONSE_SUB_SHORT_JSON_TOPIC:
					case MqttTopics.DEVICE_RPC_RESPONSE_SUB_SHORT_PROTO_TOPIC:
					case MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_TOPIC:
					case MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_SHORT_TOPIC:
					case MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_SHORT_JSON_TOPIC:
					case MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_SHORT_PROTO_TOPIC:
					case MqttTopics.GATEWAY_ATTRIBUTES_TOPIC:
					case MqttTopics.GATEWAY_RPC_TOPIC:
					case MqttTopics.GATEWAY_ATTRIBUTES_RESPONSE_TOPIC:
					case MqttTopics.DEVICE_PROVISION_RESPONSE_TOPIC:
					case MqttTopics.DEVICE_FIRMWARE_RESPONSES_TOPIC:
					case MqttTopics.DEVICE_FIRMWARE_ERROR_TOPIC:
					case MqttTopics.DEVICE_SOFTWARE_RESPONSES_TOPIC:
					case MqttTopics.DEVICE_SOFTWARE_ERROR_TOPIC:
						registerSubQoS(topic, grantedQoSList, reqQoS);
						break;
					default:
						log.warn("[{}] Failed to subscribe to [{}][{}]", sessionId, topic, reqQoS);
						grantedQoSList.add(ReturnCodeResolver.getSubscriptionReturnCode(deviceSessionCtx.getMqttVersion(), ReturnCode.TOPIC_FILTER_INVALID));
						break;
				}
			} catch (Exception e) {
				log.warn("[{}] Failed to subscribe to [{}][{}]", sessionId, topic, reqQoS, e);
				grantedQoSList.add(ReturnCodeResolver.getSubscriptionReturnCode(deviceSessionCtx.getMqttVersion(), ReturnCode.IMPLEMENTATION_SPECIFIC));
			}
		}
		if (!activityReported) {
//			transportService.recordActivity(deviceSessionCtx.getSessionInfo());
		}
		ctx.writeAndFlush(createSubAckMessage(mqttMsg.variableHeader().messageId(), grantedQoSList));
	}

	private void processAttributesSubscribe(List<Integer> grantedQoSList, String topic, MqttQoS reqQoS, TopicType topicType) {
//		transportService.process(deviceSessionCtx.getSessionInfo(), TransportProtos.SubscribeToAttributeUpdatesMsg.newBuilder().build(), null);
		attrSubTopicType = topicType;
		registerSubQoS(topic, grantedQoSList, reqQoS);
	}

	public void registerSubQoS(String topic, List<Integer> grantedQoSList, MqttQoS reqQoS) {
		grantedQoSList.add(getMinSupportedQos(reqQoS));
		mqttQoSMap.put(new MqttTopicMatcher(topic), getMinSupportedQos(reqQoS));
	}

	private static int getMinSupportedQos(MqttQoS reqQoS) {
		return Math.min(reqQoS.value(), MAX_SUPPORTED_QOS_LVL.value());
	}

	private static MqttSubAckMessage createSubAckMessage(Integer msgId, List<Integer> grantedQoSList) {
		MqttFixedHeader mqttFixedHeader =
			new MqttFixedHeader(SUBACK, false, AT_MOST_ONCE, false, 0);
		MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(msgId);
		MqttSubAckPayload mqttSubAckPayload = new MqttSubAckPayload(grantedQoSList);
		return new MqttSubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubAckPayload);
	}

	private void processRpcSubscribe(List<Integer> grantedQoSList, String topic, MqttQoS reqQoS, TopicType topicType) {
//		transportService.process(deviceSessionCtx.getSessionInfo(), TransportProtos.SubscribeToRPCMsg.newBuilder().build(), null);
//		rpcSubTopicType = topicType;
//		registerSubQoS(topic, grantedQoSList, reqQoS);
	}

	private void processPublish(ChannelHandlerContext ctx, MqttPublishMessage mqttMsg) {
		if (!checkConnected(ctx, mqttMsg)) {
			return;
		}
		String topicName = mqttMsg.variableHeader().topicName();
		int msgId = mqttMsg.variableHeader().packetId();
		log.trace("[{}][{}] Processing publish msg [{}][{}]!", sessionId, deviceSessionCtx.getDeviceId(), topicName, msgId);
		processDevicePublish(ctx, mqttMsg, topicName, msgId);
	}

	private void processDevicePublish(ChannelHandlerContext ctx, MqttPublishMessage mqttMsg, String topicName, int msgId) {
		try {
			if (deviceSessionCtx.isDeviceTelemetryTopic(topicName)) {
				//TODO
				ack(ctx, msgId, ReturnCode.SUCCESS);
			} else {
				ack(ctx, msgId, ReturnCode.TOPIC_NAME_INVALID);
			}
		} catch (Exception e) {
			log.debug("[{}] Failed to process publish msg [{}][{}]", sessionId, topicName, msgId, e);
			sendAckOrCloseSession(ctx, topicName, msgId);
		}
	}

	private void ack(ChannelHandlerContext ctx, int msgId, ReturnCode returnCode) {
		if (msgId > 0) {
			ctx.writeAndFlush(createMqttPubAckMsg(deviceSessionCtx, msgId, returnCode));
		}
	}

	private void sendAckOrCloseSession(ChannelHandlerContext ctx, String topicName, int msgId) {
		if ((deviceSessionCtx.isSendAckOnValidationException() || MqttVersion.MQTT_5.equals(deviceSessionCtx.getMqttVersion())) && msgId > 0) {
			log.debug("[{}] Send pub ack on invalid publish msg [{}][{}]", sessionId, topicName, msgId);
			ctx.writeAndFlush(createMqttPubAckMsg(deviceSessionCtx, msgId, ReturnCode.PAYLOAD_FORMAT_INVALID));
		} else {
			log.info("[{}] Closing current session due to invalid publish msg [{}][{}]", sessionId, topicName, msgId);
			closeCtx(ctx);
		}
	}

	public static MqttMessage createMqttPubAckMsg(DeviceSessionCtx deviceSessionCtx, int requestId, ReturnCode returnCode) {
		MqttMessageBuilders.PubAckBuilder pubAckMsgBuilder = MqttMessageBuilders.pubAck().packetId(requestId);
		if (MqttVersion.MQTT_5.equals(deviceSessionCtx.getMqttVersion())) {
			pubAckMsgBuilder.reasonCode(returnCode.byteValue());
		}
		return pubAckMsgBuilder.build();
	}

	private boolean checkConnected(ChannelHandlerContext ctx, MqttMessage msg) {
		if (deviceSessionCtx.isConnected()) {
			return true;
		} else {
			log.info("[{}] Closing current session due to invalid msg order: {}", sessionId, msg);
			return false;
		}
	}

	void processConnect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
		String userName = msg.payload().userName();
		log.debug("[{}][{}] Processing connect msg for client: {}!", address, sessionId, userName);

		deviceSessionCtx.setMqttVersion(getMqttVersion(msg.variableHeader().version()));

		X509Certificate cert;
		if (sslHandler != null && (cert = getX509Certificate()) != null) {
			processX509CertConnect(ctx, cert, msg);
		} else {
			processAuthTokenConnect(ctx, msg);
		}
	}

	private void processAuthTokenConnect(ChannelHandlerContext ctx, MqttConnectMessage connectMessage) {
		String userName = connectMessage.payload().userName();
		log.debug("[{}][{}] Processing connect msg for client with user name: {}!", address, sessionId, userName);

		ValidateBasicMqttCredRequestMsg.ValidateBasicMqttCredRequestMsgBuilder request = ValidateBasicMqttCredRequestMsg.builder()
			.clientId(connectMessage.payload().clientIdentifier());
		if (userName != null) {
			request.userName(userName);
		}
		byte[] passwordBytes = connectMessage.payload().passwordInBytes();
		if (passwordBytes != null) {
			String password = new String(passwordBytes, CharsetUtil.UTF_8);
			request.password(password);
		}

		transportService.process(DeviceTransportType.MQTT, request.build(),
			new TransportServiceCallback<>() {
				@Override
				public void onSuccess(ValidateDeviceCredentialsResponse msg) {
					onValidateDeviceResponse(msg, ctx, connectMessage);
				}

				@Override
				public void onError(Throwable e) {
					log.trace("[{}] Failed to process credentials: {}", address, userName, e);
					ctx.writeAndFlush(createMqttConnAckMsg(ReturnCode.SERVER_UNAVAILABLE_5, connectMessage));
					closeCtx(ctx);
				}
			});
	}

	private void onValidateDeviceResponse(ValidateDeviceCredentialsResponse msg, ChannelHandlerContext ctx, MqttConnectMessage connectMessage) {
		if (!msg.hasDeviceInfo()) {
			context.onAuthFailure(address);
			ReturnCode returnCode = ReturnCode.NOT_AUTHORIZED_5;
			if (sslHandler == null || getX509Certificate() == null) {
				String username = connectMessage.payload().userName();
				byte[] passwordBytes = connectMessage.payload().passwordInBytes();
				String clientId = connectMessage.payload().clientIdentifier();
				if ((username != null && passwordBytes != null && clientId != null)
					|| (username == null ^ passwordBytes == null)) {
					returnCode = ReturnCode.BAD_USERNAME_OR_PASSWORD;
				} else if (!StringUtils.isBlank(clientId)) {
					returnCode = ReturnCode.CLIENT_IDENTIFIER_NOT_VALID;
				}
			}
			ctx.writeAndFlush(createMqttConnAckMsg(returnCode, connectMessage));
			closeCtx(ctx);
		} else {
			context.onAuthSuccess(address);
			deviceSessionCtx.setDeviceInfo(msg.getDeviceInfo());
			deviceSessionCtx.setDeviceProfile(msg.getDeviceProfile());

			ctx.writeAndFlush(createMqttConnAckMsg(ReturnCode.SUCCESS, connectMessage));
			deviceSessionCtx.setConnected(true);
			log.info("[{}] Client connected!", sessionId);
		}
	}

	private void processX509CertConnect(ChannelHandlerContext ctx, X509Certificate cert, MqttConnectMessage connectMessage) {
		try {
			if (!context.isSkipValidityCheckForClientCert()) {
				cert.checkValidity();
			}
			String strCert = SslUtil.getCertificateString(cert);
			String sha3Hash = EncryptionUtil.getSha3Hash(strCert);
//			transportService.process(DeviceTransportType.MQTT, ValidateDeviceX509CertRequestMsg.newBuilder().setHash(sha3Hash).build(),
//				new TransportServiceCallback<>() {
//					@Override
//					public void onSuccess(ValidateDeviceCredentialsResponse msg) {
//						onValidateDeviceResponse(msg, ctx, connectMessage);
//					}
//
//					@Override
//					public void onError(Throwable e) {
//						log.trace("[{}] Failed to process credentials: {}", address, sha3Hash, e);
//						ctx.writeAndFlush(createMqttConnAckMsg(ReturnCode.SERVER_UNAVAILABLE_5, connectMessage));
//						closeCtx(ctx);
//					}
//				});
		} catch (Exception e) {
			context.onAuthFailure(address);
			ctx.writeAndFlush(createMqttConnAckMsg(ReturnCode.NOT_AUTHORIZED_5, connectMessage));
			log.trace("[{}] X509 auth failure: {}", sessionId, address, e);
			closeCtx(ctx);
		}
	}

	private X509Certificate getX509Certificate() {
		try {
			Certificate[] certChain = sslHandler.engine().getSession().getPeerCertificates();
			if (certChain.length > 0) {
				return (X509Certificate) certChain[0];
			}
		} catch (SSLPeerUnverifiedException e) {
			log.warn(e.getMessage());
			return null;
		}
		return null;
	}


	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		context.channelUnregistered();
	}

	@Override
	public void operationComplete(Future<? super Void> future) throws Exception {
		log.info("[{}] Channel closed!", sessionId);
		doDisconnect();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		if (cause instanceof IOException) {
			if (log.isDebugEnabled()) {
				log.debug("[{}][{}][{}] IOException: {}", sessionId,
					Optional.ofNullable(this.deviceSessionCtx.getDeviceInfo()).map(TransportDeviceInfo::getDeviceId).orElse(null),
					Optional.ofNullable(this.deviceSessionCtx.getDeviceInfo()).map(TransportDeviceInfo::getDeviceName).orElse(""),
					cause);
			} else if (log.isInfoEnabled()) {
				log.info("[{}][{}][{}] IOException: {}", sessionId,
					Optional.ofNullable(this.deviceSessionCtx.getDeviceInfo()).map(TransportDeviceInfo::getDeviceId).orElse(null),
					Optional.ofNullable(this.deviceSessionCtx.getDeviceInfo()).map(TransportDeviceInfo::getDeviceName).orElse(""),
					cause.getMessage());
			}
		} else {
			log.error("[{}] Unexpected Exception", sessionId, cause);
		}

		closeCtx(ctx);
		if (cause instanceof OutOfMemoryError) {
			log.error("Received critical error. Going to shutdown the service.");
			System.exit(1);
		}
	}

	public void doDisconnect() {
//		if (deviceSessionCtx.isConnected()) {
//			log.debug("[{}] Client disconnected!", sessionId);
//			transportService.process(deviceSessionCtx.getSessionInfo(), SESSION_EVENT_MSG_CLOSED, null);
//			transportService.deregisterSession(deviceSessionCtx.getSessionInfo());
//			if (gatewaySessionHandler != null) {
//				gatewaySessionHandler.onDevicesDisconnect();
//			}
//			deviceSessionCtx.setDisconnected();
//		}
//		deviceSessionCtx.release();
	}

	private MqttConnAckMessage createMqttConnAckMsg(ReturnCode returnCode, MqttConnectMessage msg) {
		MqttMessageBuilders.ConnAckBuilder connAckBuilder = MqttMessageBuilders.connAck();
		connAckBuilder.sessionPresent(!msg.variableHeader().isCleanSession());
		MqttConnectReturnCode finalReturnCode = ReturnCodeResolver.getConnectionReturnCode(deviceSessionCtx.getMqttVersion(), returnCode);
		connAckBuilder.returnCode(finalReturnCode);
		return connAckBuilder.build();
	}

	private static MqttVersion getMqttVersion(int versionCode) {
		switch (versionCode) {
			case 3:
				return MqttVersion.MQTT_3_1;
			case 5:
				return MqttVersion.MQTT_5;
			default:
				return MqttVersion.MQTT_3_1_1;
		}
	}

	InetSocketAddress getAddress(ChannelHandlerContext ctx) {
		var address = ctx.channel().attr(ADDRESS).get();
		if (address == null) {
			log.trace("[{}] Received empty address.", ctx.channel().id());
			InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			log.trace("[{}] Going to use address: {}", ctx.channel().id(), remoteAddress);
			return remoteAddress;
		} else {
			log.trace("[{}] Received address: {}", ctx.channel().id(), address);
		}
		return address;
	}

	private void closeCtx(ChannelHandlerContext ctx) {
//		if (!rpcAwaitingAck.isEmpty()) {
//			log.debug("[{}] Cleanup RPC awaiting ack map due to session close!", sessionId);
//			rpcAwaitingAck.clear();
//		}
		ctx.close();
	}


}
