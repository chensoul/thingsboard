package com.chensoul.netty.thingsboard.mqtt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thingsboard.common.stats.StatsFactory;
import org.thingsboard.common.stats.StatsType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultTransportService implements TransportService {
	private final Map<String, Number> statsMap = new LinkedHashMap<>();

	@Value("${transport.stats.enabled:false}")
	private boolean statsEnabled;

	@Nullable
	private final StatsFactory statsFactory;

	@Override
	public void createGaugeStats(String statsName, AtomicInteger number) {
		if (statsEnabled && statsFactory != null) {
			statsFactory.createGauge(StatsType.TRANSPORT + "." + statsName, number);
			statsMap.put(statsName, number);
		}
	}

	@Scheduled(fixedDelayString = "${transport.stats.print-interval-ms:60000}")
	public void printStats() {
		if (statsEnabled && !statsMap.isEmpty()) {
			String values = statsMap.entrySet().stream()
				.map(kv -> kv.getKey() + " [" + kv.getValue() + "]").collect(Collectors.joining(", "));
			log.info("Transport Stats: {}", values);
		}
	}
}
