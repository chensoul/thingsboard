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
