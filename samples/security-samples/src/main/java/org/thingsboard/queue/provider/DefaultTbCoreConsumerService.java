package org.thingsboard.queue.provider;

import com.google.common.collect.Sets;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thingsboard.common.concurrent.ThingsBoardThreadFactory;
import org.thingsboard.queue.common.TbProtoQueueMsg;
import org.thingsboard.queue.msg.IdMsgPair;
import org.thingsboard.queue.msg.TbCallback;
import org.thingsboard.queue.msg.TbPackCallback;
import org.thingsboard.queue.msg.TbPackProcessingContext;
import org.thingsboard.queue.msg.ToCoreMsg;
import org.thingsboard.queue.msg.TopicPartitionInfo;
import org.thingsboard.queue.spi.TbQueueConsumer;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultTbCoreConsumerService {
	private final TbCoreQueueFactory tbCoreQueueFactory;
	private TbQueueConsumer<TbProtoQueueMsg<ToCoreMsg>> mainConsumer;
	protected volatile ExecutorService consumersExecutor;

	@Value("${queue.core.poll-interval}")
	private long pollDuration;
	@Value("${queue.core.pack-processing-timeout}")
	private long packProcessingTimeout;

	protected volatile boolean stopped = false;

	@PostConstruct
	public void init() {
		this.mainConsumer = tbCoreQueueFactory.createToCoreMsgConsumer();
		//
		this.mainConsumer.subscribe(Sets.newHashSet(TopicPartitionInfo.builder().topic("tb_core").build()));

		this.consumersExecutor = Executors.newCachedThreadPool(ThingsBoardThreadFactory.forName("tb-core-consumer"));

		launchMainConsumers();
	}

	protected void launchMainConsumers() {
		consumersExecutor.submit(() -> {
			while (!stopped) {
				try {
					List<TbProtoQueueMsg<ToCoreMsg>> msgs = mainConsumer.poll(pollDuration);
					if (msgs.isEmpty()) {
						continue;
					}
					List<IdMsgPair<ToCoreMsg>> orderedMsgList = msgs.stream().map(msg -> new IdMsgPair<>(UUID.randomUUID(), msg)).collect(Collectors.toList());
					ConcurrentMap<UUID, TbProtoQueueMsg<ToCoreMsg>> pendingMap = orderedMsgList.stream().collect(
						Collectors.toConcurrentMap(IdMsgPair::getUuid, IdMsgPair::getMsg));
					CountDownLatch processingTimeoutLatch = new CountDownLatch(1);
					TbPackProcessingContext<TbProtoQueueMsg<ToCoreMsg>> ctx = new TbPackProcessingContext<>(
						processingTimeoutLatch, pendingMap, new ConcurrentHashMap<>());
					PendingMsgHolder pendingMsgHolder = new PendingMsgHolder();
					Future<?> packSubmitFuture = consumersExecutor.submit(() -> {
						orderedMsgList.forEach((element) -> {
							UUID id = element.getUuid();
							TbProtoQueueMsg<ToCoreMsg> msg = element.getMsg();
							log.trace("[{}] Creating main callback for message: {}", id, msg.getValue());
							TbCallback callback = new TbPackCallback<>(id, ctx);
							try {
								ToCoreMsg toCoreMsg = msg.getValue();

								log.info("Processing message: {}", toCoreMsg.toString());

								pendingMsgHolder.setToCoreMsg(toCoreMsg);

							} catch (Throwable e) {
								log.warn("[{}] Failed to process message: {}", id, msg, e);
								callback.onFailure(e);
							}
						});
					});
					if (!processingTimeoutLatch.await(packProcessingTimeout, TimeUnit.MILLISECONDS)) {
						if (!packSubmitFuture.isDone()) {
							packSubmitFuture.cancel(true);
							ToCoreMsg lastSubmitMsg = pendingMsgHolder.getToCoreMsg();
							log.info("Timeout to process message: {}", lastSubmitMsg);
						}
						ctx.getAckMap().forEach((id, msg) -> log.info("[{}] Timeout to process message: {}", id, msg.getValue()));
						ctx.getFailedMap().forEach((id, msg) -> log.info("[{}] Failed to process message: {}", id, msg.getValue()));
					}
					mainConsumer.commit();
				} catch (Exception e) {
					if (!stopped) {
						log.warn("Failed to obtain messages from queue.", e);
						try {
							Thread.sleep(pollDuration);
						} catch (InterruptedException e2) {
							log.trace("Failed to wait until the server has capacity to handle new requests", e2);
						}
					}
				}
			}
			log.info("TB Core Consumer stopped.");
		});
	}

	private static class PendingMsgHolder {
		@Getter
		@Setter
		private volatile ToCoreMsg toCoreMsg;
	}

}
