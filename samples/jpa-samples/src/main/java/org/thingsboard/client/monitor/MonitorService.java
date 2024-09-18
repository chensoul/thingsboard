package org.thingsboard.client.monitor;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.thingsboard.client.ws.WsClient;
import org.thingsboard.client.ws.WsClientFactory;
import org.thingsboard.common.concurrent.ThreadFactory;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.ws.cmd.AttributeCmd;
import org.thingsboard.server.ws.cmd.WsCmdWrapper;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorService {
	private final TbClient tbClient;
	private final WsClientFactory wsClientFactory;
	private StopWatch stopWatch = new StopWatch();
	@Value("${monitoring.monitoring_rate_ms}")
	private int monitoringRateMs;

	public final void runChecks() {
		try {
			log.info("Starting runChecks");

			stopWatch.start();
			String accessToken = tbClient.getToken();
			try (WsClient wsClient = wsClientFactory.createClient(accessToken)) {
				wsClient.registerWaitForUpdate();
				wsClient.send(JacksonUtil.toString(new WsCmdWrapper(null, Arrays.asList(new AttributeCmd(1)))));
				JsonNode reply = wsClient.waitForReply();
				if (reply != null) {
					log.info("Received reply: {}", reply);
				}
			}
			stopWatch.stop();
			log.debug("Finished runChecks");
		} catch (Throwable error) {
			log.error("Failed to run checks", error);
		}
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startMonitoring() {
		tbClient.logIn();

		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(ThreadFactory.forName("monitor"));
		scheduler.scheduleWithFixedDelay(() -> {
			runChecks();
		}, 0, monitoringRateMs, TimeUnit.MILLISECONDS);
	}

}
