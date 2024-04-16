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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.ResourceLeakDetector;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thingsboard.common.model.DataConstants;
import org.thingsboard.transport.TbTransportService;

/**
 * @author Andrew Shvayka
 */
@Service("MqttTransportService")
public class MqttTransportService implements TbTransportService {
	public static AttributeKey<InetSocketAddress> ADDRESS = AttributeKey.newInstance("SRC_ADDRESS");

	private Logger log = LoggerFactory.getLogger(MqttTransportService.class);

	@Value("${transport.mqtt.bind_address}")
	private String host;
	@Value("${transport.mqtt.bind_port}")
	private Integer port;

	@Value("${transport.mqtt.ssl.enabled}")
	private boolean sslEnabled;
	@Value("${transport.mqtt.ssl.bind_address}")
	private String sslHost;
	@Value("${transport.mqtt.ssl.bind_port}")
	private Integer sslPort;

	@Value("${transport.mqtt.netty.leak_detector_level}")
	private String leakDetectorLevel;
	@Value("${transport.mqtt.netty.boss_group_thread_count}")
	private Integer bossGroupThreadCount;
	@Value("${transport.mqtt.netty.worker_group_thread_count}")
	private Integer workerGroupThreadCount;
	@Value("${transport.mqtt.netty.so_keep_alive}")
	private boolean keepAlive;

	@Autowired
	private MqttTransportContext context;

	private Channel serverChannel;
	private Channel sslServerChannel;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	@PostConstruct
	public void init() throws Exception {
		log.info("Setting resource leak detector level to {}", leakDetectorLevel);
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.valueOf(leakDetectorLevel.toUpperCase()));

		log.info("Starting MQTT transport...");
		bossGroup = new NioEventLoopGroup(bossGroupThreadCount);
		workerGroup = new NioEventLoopGroup(workerGroupThreadCount);

		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new MqttTransportServerInitializer(context, false))
			.childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
		;
		serverChannel = b.bind(host, port).sync().channel();

		if (sslEnabled) {
			b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new MqttTransportServerInitializer(context, true))
				.childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
			;
			sslServerChannel = b.bind(sslHost, sslPort).sync().channel();
		}

		log.info("Mqtt transport started at port {}", sslEnabled ? sslPort : port);
	}

	@PreDestroy
	public void shutdown() throws InterruptedException {
		log.info("Stopping MQTT transport!");
		try {
			serverChannel.close().sync();
			if (sslEnabled) {
				sslServerChannel.close().sync();
			}
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		log.info("MQTT transport stopped!");
	}

	@Override
	public String getName() {
		return DataConstants.MQTT_TRANSPORT_NAME;
	}
}
