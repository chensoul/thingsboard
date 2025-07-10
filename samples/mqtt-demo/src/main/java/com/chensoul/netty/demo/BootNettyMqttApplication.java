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

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BootNettyMqttApplication implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BootNettyMqttApplication.class);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		int port = 1885;
		BootNettyMqttServerThread bootNettyMqttServerThread = new BootNettyMqttServerThread(port);
		bootNettyMqttServerThread.start();
	}
}
