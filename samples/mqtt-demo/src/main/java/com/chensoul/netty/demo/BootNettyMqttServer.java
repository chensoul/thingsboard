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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

public class BootNettyMqttServer {
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workGroup;

	/**
	 * 启动服务
	 */
	public void startup(int port) {
		try {
			bossGroup = new NioEventLoopGroup(1);
			workGroup = new NioEventLoopGroup();

			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workGroup);
			bootstrap.channel(NioServerSocketChannel.class);

//			bootstrap.option(ChannelOption.SO_REUSEADDR, true)
//				.option(ChannelOption.SO_BACKLOG, 1024)
//				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
//				.option(ChannelOption.SO_RCVBUF, 10485760);
//
			bootstrap
//			.childOption(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.SO_KEEPALIVE, false)
//				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			;

			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel ch) {
					ChannelPipeline channelPipeline = ch.pipeline();
					// 设置读写空闲超时时间
//					channelPipeline.addLast(new IdleStateHandler(600, 600, 1200));
					channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
					channelPipeline.addLast("decoder", new MqttDecoder());
					channelPipeline.addLast(new BootNettyMqttChannelInboundHandler());
				}
			});
			ChannelFuture f = bootstrap.bind(port).sync();
//			if (f.isSuccess()) {
//				System.out.println("startup success port = " + port);
//				f.channel().closeFuture().sync();
//			} else {
//				System.out.println("startup fail port = " + port);
//			}
//			//	绑定端口,监听服务是否启动
//			bootstrap.bind(port).addListener((ChannelFutureListener) channelFuture -> {
//				if (channelFuture.isSuccess()) {
//					System.out.println("startup success --- ");
//				} else {
//					System.out.println("startup fail ---");
//				}
//			});
		} catch (Exception e) {
			System.out.println("start exception" + e.toString());
		}
	}

	/**
	 * 关闭服务
	 */
	public void shutdown() throws InterruptedException {
		if (workGroup != null && bossGroup != null) {
			bossGroup.shutdownGracefully().sync();
			workGroup.shutdownGracefully().sync();
			System.out.println("shutdown success");
		}
	}

}
