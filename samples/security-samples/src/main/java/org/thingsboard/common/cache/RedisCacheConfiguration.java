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

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.support.DefaultFormattingConversionService;
import static org.thingsboard.common.StringConstants.COLON;
import static org.thingsboard.common.StringConstants.COMMA;
import org.thingsboard.common.util.SslUtil;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ConditionalOnProperty(prefix = "cache", value = "type", havingValue = "redis")
@EnableCaching
@Data
@Slf4j
public abstract class RedisCacheConfiguration {

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

	@Value("${redis.ssl.enabled:false}")
	private boolean sslEnabled;

	@Value("${cache.stats.enabled:true}")
	private boolean cacheStatsEnabled;

	@Autowired
	private RedisSslCredential redisSslCredentials;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return loadFactory();
	}

	protected abstract JedisConnectionFactory loadFactory();

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory cf) {
		DefaultFormattingConversionService redisConversionService = new DefaultFormattingConversionService();
		org.springframework.data.redis.cache.RedisCacheConfiguration.registerDefaultConverters(redisConversionService);
		org.springframework.data.redis.cache.RedisCacheConfiguration configuration = createRedisCacheConfig(redisConversionService);

		Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

		log.info("Initialized redis cache specs {}", cacheSpecsMap.getSpecs());

		if (cacheSpecsMap != null) {
			cacheSpecsMap.getSpecs().forEach((cacheName, cacheSpecs) -> {
				cacheConfigurations.put(cacheName, createRedisCacheConfigWithTtl(redisConversionService, cacheSpecs.getTimeToLiveInMinutes()));
			});
		}

		var redisCacheManagerBuilder = RedisCacheManager.builder(cf).cacheDefaults(configuration).withInitialCacheConfigurations(cacheConfigurations).transactionAware();
		if (cacheStatsEnabled) {
			redisCacheManagerBuilder.enableStatistics();
		}
		return redisCacheManagerBuilder.build();
	}

	private org.springframework.data.redis.cache.RedisCacheConfiguration createRedisCacheConfigWithTtl(DefaultFormattingConversionService redisConversionService, int ttlInMinutes) {
		return createRedisCacheConfig(redisConversionService).entryTtl(Duration.ofMinutes(ttlInMinutes));
	}

	private org.springframework.data.redis.cache.RedisCacheConfiguration createRedisCacheConfig(DefaultFormattingConversionService redisConversionService) {
		return org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig().withConversionService(redisConversionService);
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

	protected SSLSocketFactory createSslSocketFactory() {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			KeyManagerFactory keyManagerFactory = createAndInitKeyManagerFactory();
			TrustManagerFactory trustManagerFactory = createAndInitTrustManagerFactory();
			sslContext.init(keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			return sslContext.getSocketFactory();
		} catch (Exception e) {
			throw new RuntimeException("Creating TLS factory failed!", e);
		}
	}

	private TrustManagerFactory createAndInitTrustManagerFactory() throws Exception {
		List<X509Certificate> caCerts = SslUtil.readCertFileByPath(redisSslCredentials.getCertFile());
		KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		caKeyStore.load(null, null);
		for (X509Certificate caCert : caCerts) {
			caKeyStore.setCertificateEntry("redis-caCert-cert-" + caCert.getSubjectX500Principal().getName(), caCert);
		}

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(caKeyStore);
		return trustManagerFactory;
	}

	private KeyManagerFactory createAndInitKeyManagerFactory() throws Exception {
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(loadKeyStore(), null);
		return kmf;
	}

	private KeyStore loadKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
		if (redisSslCredentials.getUserCertFile().isBlank() || redisSslCredentials.getUserKeyFile().isBlank()) {
			return null;
		}
		List<X509Certificate> certificates = SslUtil.readCertFileByPath(redisSslCredentials.getCertFile());
		PrivateKey privateKey = SslUtil.readPrivateKeyByFilePath(redisSslCredentials.getUserKeyFile(), null);

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null);
		List<X509Certificate> unique = certificates.stream().distinct().toList();
		for (X509Certificate cert : unique) {
			keyStore.setCertificateEntry("redis-cert" + cert.getSubjectX500Principal().getName(), cert);
		}

		if (privateKey != null) {
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			CertPath certPath = factory.generateCertPath(certificates);
			List<? extends Certificate> path = certPath.getCertificates();
			Certificate[] x509Certificates = path.toArray(new Certificate[0]);
			keyStore.setKeyEntry("redis-private-key", privateKey, null, x509Certificates);
		}
		return keyStore;
	}
}
