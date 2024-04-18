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
package org.thingsboard.common.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.support.DefaultFormattingConversionService;
import static org.thingsboard.common.CacheConstants.ALL_CACHES;
import static org.thingsboard.common.StringConstants.COLON;
import static org.thingsboard.common.StringConstants.COMMA;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ConditionalOnProperty(prefix = "cache", value = "type", havingValue = "redis")
@EnableCaching
@Data
public abstract class TBRedisCacheConfiguration {

	private final CacheSpecsMap cacheSpecsMap;

	@Value("${redis.pool_config.maxTotal:128}")
	private int maxTotal;

	@Value("${redis.pool_config.maxIdle:128}")
	private int maxIdle;

	@Value("${redis.pool_config.minIdle:16}")
	private int minIdle;

	@Value("${redis.pool_config.testOnBorrow:true}")
	private boolean testOnBorrow;

	@Value("${redis.pool_config.testOnReturn:true}")
	private boolean testOnReturn;

	@Value("${redis.pool_config.testWhileIdle:true}")
	private boolean testWhileIdle;

	@Value("${redis.pool_config.minEvictableMs:60000}")
	private long minEvictableMs;

	@Value("${redis.pool_config.evictionRunsMs:30000}")
	private long evictionRunsMs;

	@Value("${redis.pool_config.maxWaitMills:60000}")
	private long maxWaitMills;

	@Value("${redis.pool_config.numberTestsPerEvictionRun:3}")
	private int numberTestsPerEvictionRun;

	@Value("${redis.pool_config.blockWhenExhausted:true}")
	private boolean blockWhenExhausted;

	@Value("${cache.stats.enabled:true}")
	private boolean cacheStatsEnabled;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return loadFactory();
	}

	protected abstract JedisConnectionFactory loadFactory();

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory cf) {
		DefaultFormattingConversionService redisConversionService = new DefaultFormattingConversionService();
		RedisCacheConfiguration.registerDefaultConverters(redisConversionService);
		RedisCacheConfiguration configuration = createRedisCacheConfig(redisConversionService);

		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		if (cacheSpecsMap != null) {
			cacheSpecsMap.getSpecs().forEach((cacheName, cacheSpecs) -> {
				cacheConfigurations.put(cacheName, createRedisCacheConfigWithTtl(redisConversionService, cacheSpecs.getTimeToLiveInMinutes()));
			});
			List<String> copiedCaches = new ArrayList<>(ALL_CACHES);
			copiedCaches.removeAll(cacheSpecsMap.getSpecs().keySet());
			copiedCaches.forEach(cacheName -> {
				cacheConfigurations.put(cacheName, createRedisCacheConfigWithTtl(redisConversionService, cacheSpecsMap.getTimeToLiveInMinutes()));
			});
		}

		var redisCacheManagerBuilder = RedisCacheManager.builder(cf).cacheDefaults(configuration).withInitialCacheConfigurations(cacheConfigurations).transactionAware();
		if (cacheStatsEnabled) {
			redisCacheManagerBuilder.enableStatistics();
		}
		return redisCacheManagerBuilder.build();
	}

	private RedisCacheConfiguration createRedisCacheConfigWithTtl(DefaultFormattingConversionService redisConversionService, int ttlInMinutes) {
		return createRedisCacheConfig(redisConversionService).entryTtl(Duration.ofMinutes(ttlInMinutes));
	}

	private RedisCacheConfiguration createRedisCacheConfig(DefaultFormattingConversionService redisConversionService) {
		return RedisCacheConfiguration.defaultCacheConfig().withConversionService(redisConversionService);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory());
		return template;
	}

	protected JedisPoolConfig buildPoolConfig() {
		final JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setTestOnBorrow(testOnBorrow);
		poolConfig.setTestOnReturn(testOnReturn);
		poolConfig.setTestWhileIdle(testWhileIdle);
		poolConfig.setSoftMinEvictableIdleTime(Duration.ofMillis(minEvictableMs));
		poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(evictionRunsMs));
		poolConfig.setMaxWait(Duration.ofMillis(maxWaitMills));
		poolConfig.setNumTestsPerEvictionRun(numberTestsPerEvictionRun);
		poolConfig.setBlockWhenExhausted(blockWhenExhausted);
		return poolConfig;
	}

	protected List<RedisNode> getNodes(String nodes) {
		List<RedisNode> result;
		if (StringUtils.isBlank(nodes)) {
			result = Collections.emptyList();
		} else {
			result = new ArrayList<>();
			for (String hostPort : nodes.split(COMMA)) {
				String host = hostPort.split(COLON)[0];
				int port = Integer.parseInt(hostPort.split(COLON)[1]);
				result.add(new RedisNode(host, port));
			}
		}
		return result;
	}
}
