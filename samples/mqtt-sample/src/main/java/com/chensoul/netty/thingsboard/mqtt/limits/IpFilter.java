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
package com.chensoul.netty.thingsboard.mqtt.limits;

import com.chensoul.netty.thingsboard.mqtt.MqttTransportContext;
import static com.chensoul.netty.thingsboard.mqtt.MqttTransportServerInitializer.ADDRESS;
import com.chensoul.netty.thingsboard.mqtt.MqttTransportStarter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpFilter extends AbstractRemoteAddressFilter<InetSocketAddress> {
	private MqttTransportContext context;

	public IpFilter(MqttTransportContext context) {
		this.context = context;
	}

	@Override
	protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
		log.info("[{}] Received msg: {}", ctx.channel().id(), remoteAddress);
		if (context.checkAddress(remoteAddress)) {
			log.info("[{}] Setting address: {}", ctx.channel().id(), remoteAddress);
			ctx.channel().attr(ADDRESS).set(remoteAddress);
			return true;
		} else {
			return false;
		}
	}
}
