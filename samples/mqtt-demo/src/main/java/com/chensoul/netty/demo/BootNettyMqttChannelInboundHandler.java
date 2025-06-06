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
package com.chensoul.netty.demo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@ChannelHandler.Sharable
public class BootNettyMqttChannelInboundHandler extends ChannelInboundHandlerAdapter {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 从客户端收到新的数据时，这个方法会在收到消息时被调用
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception, IOException {
//		if (null != msg) {
//			MqttMessage mqttMessage = (MqttMessage) msg;
//			log.info("info--" + mqttMessage.toString());
//			MqttFixedHeader mqttFixedHeader = mqttMessage.fixedHeader();
//			Channel channel = ctx.channel();
//
//			if (mqttFixedHeader.messageType().equals(MqttMessageType.CONNECT)) {
//				//	在一个网络连接上，客户端只能发送一次CONNECT报文。服务端必须将客户端发送的第二个CONNECT报文当作协议违规处理并断开客户端的连接
//				//	to do 建议connect消息单独处理，用来对客户端进行认证管理等 这里直接返回一个CONNACK消息
//				BootNettyMqttMsgBack.connack(channel, mqttMessage);
//			}
//
//			switch (mqttFixedHeader.messageType()) {
//				case PUBLISH:        //	客户端发布消息
//					//	PUBACK报文是对QoS 1等级的PUBLISH报文的响应
//					System.out.println("123");
//					BootNettyMqttMsgBack.puback(channel, mqttMessage);
//					break;
//				case PUBREL:        //	发布释放
//					//	PUBREL报文是对PUBREC报文的响应
//					//	to do
//					BootNettyMqttMsgBack.pubcomp(channel, mqttMessage);
//					break;
//				case SUBSCRIBE:        //	客户端订阅主题
//					//	客户端向服务端发送SUBSCRIBE报文用于创建一个或多个订阅，每个订阅注册客户端关心的一个或多个主题。
//					//	为了将应用消息转发给与那些订阅匹配的主题，服务端发送PUBLISH报文给客户端。
//					//	SUBSCRIBE报文也（为每个订阅）指定了最大的QoS等级，服务端根据这个发送应用消息给客户端
//					// 	to do
//					BootNettyMqttMsgBack.suback(channel, mqttMessage);
//					break;
//				case UNSUBSCRIBE:    //	客户端取消订阅
//					//	客户端发送UNSUBSCRIBE报文给服务端，用于取消订阅主题
//					//	to do
//					BootNettyMqttMsgBack.unsuback(channel, mqttMessage);
//					break;
//				case PINGREQ:        //	客户端发起心跳
//					//	客户端发送PINGREQ报文给服务端的
//					//	在没有任何其它控制报文从客户端发给服务的时，告知服务端客户端还活着
//					//	请求服务端发送 响应确认它还活着，使用网络以确认网络连接没有断开
//					BootNettyMqttMsgBack.pingresp(channel, mqttMessage);
//					break;
//				case DISCONNECT:    //	客户端主动断开连接
//					//	DISCONNECT报文是客户端发给服务端的最后一个控制报文， 服务端必须验证所有的保留位都被设置为0
//					//	to do
//					break;
//				default:
//					break;
//			}
//		}
	}
}
