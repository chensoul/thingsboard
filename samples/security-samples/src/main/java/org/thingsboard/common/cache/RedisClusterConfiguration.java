/**
 * Copyright © 2016-2024 The Thingsboard Authors
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
package org.thingsboard.common.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

@Configuration
@ConditionalOnMissingBean(CaffeineCacheConfiguration.class)
@ConditionalOnProperty(prefix = "redis.connection", value = "type", havingValue = "cluster")
public class RedisClusterConfiguration extends RedisCacheConfiguration {

	public RedisClusterConfiguration(CacheSpecsMap cacheSpecsMap) {
        super(cacheSpecsMap);
    }

    @Value("${redis.cluster.nodes:}")
    private String clusterNodes;

    @Value("${redis.cluster.maxRedirects:12}")
    private int maxRedirects;

    @Value("${redis.cluster.useDefaultPoolConfig:true}")
    private boolean useDefaultPoolConfig;

    @Value("${redis.password:}")
    private String password;

	@Value("${redis.ssl.enabled:false}")
	private boolean useSsl;

	public JedisConnectionFactory loadFactory() {
		org.springframework.data.redis.connection.RedisClusterConfiguration clusterConfiguration = new org.springframework.data.redis.connection.RedisClusterConfiguration();
		clusterConfiguration.setClusterNodes(getNodes(clusterNodes));
		clusterConfiguration.setMaxRedirects(maxRedirects);
		clusterConfiguration.setPassword(password);
		return new JedisConnectionFactory(clusterConfiguration, buildClientConfig());
	}

	private JedisClientConfiguration buildClientConfig() {
		JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfigurationBuilder = JedisClientConfiguration.builder();
		if (!useDefaultPoolConfig) {
			jedisClientConfigurationBuilder
				.usePooling()
				.poolConfig(buildPoolConfig());
		}
		if (useSsl) {
			jedisClientConfigurationBuilder
				.useSsl()
				.sslSocketFactory(createSslSocketFactory());
		}
		return jedisClientConfigurationBuilder.build();
	}
}
