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
package org.thingsboard.domain.audit.sink;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.concurrent.ThingsBoardThreadFactory;
import org.thingsboard.domain.audit.model.AuditLog;

@Component
@ConditionalOnProperty(prefix = "audit-log.sink", value = "type", havingValue = "elasticsearch")
@Slf4j
public class ElasticsearchAuditLogSink implements AuditLogSink {
	private static final String TENANT_PLACEHOLDER = "@{TENANT}";
	private static final String DATE_PLACEHOLDER = "@{DATE}";
	private static final String INDEX_TYPE = "audit_log";

	@Value("${audit-log.sink.index_pattern}")
	private String indexPattern;
	@Value("${audit-log.sink.scheme_name}")
	private String schemeName;
	@Value("${audit-log.sink.host}")
	private String host;
	@Value("${audit-log.sink.port}")
	private int port;
	@Value("${audit-log.sink.user_name}")
	private String userName;
	@Value("${audit-log.sink.password}")
	private String password;
	@Value("${audit-log.sink.date_format}")
	private String dateFormat;

	private RestClient restClient;
	private ExecutorService executor;

	@PostConstruct
	public void init() {
		try {
			log.info("Adding elastic rest endpoint... host [{}], port [{}], scheme name [{}]",
				host, port, schemeName);
			RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, schemeName));

			if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
				log.info("...using username [{}] and password ***", userName);
				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(userName, password));
				builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
			}

			this.restClient = builder.build();
			this.executor = Executors.newSingleThreadExecutor(ThingsBoardThreadFactory.forName("elasticsearch-audit-log"));
		} catch (Exception e) {
			log.error("Sink init failed!", e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@PreDestroy
	private void destroy() {
		if (executor != null) {
			executor.shutdownNow();
		}
	}

	@Override
	public void logAction(AuditLog auditLog) {
		executor.execute(() -> {
			try {
				doLogAction(auditLog);
			} catch (Exception e) {
				log.error("Failed to log action", e);
			}
		});
	}

	private void doLogAction(AuditLog auditLog) {
		String jsonContent = createElasticJsonRecord(auditLog);

		HttpEntity entity = new NStringEntity(jsonContent, ContentType.APPLICATION_JSON);

		restClient.performRequestAsync(
			HttpMethod.POST.name(),
			String.format("/%s/%s", getIndexName(auditLog.getTenantId()), INDEX_TYPE),
			Collections.emptyMap(),
			entity,
			responseListener);
	}

	private String createElasticJsonRecord(AuditLog auditLog) {
		ObjectNode auditLogNode = JacksonUtil.newObjectNode();
		auditLogNode.put("id", auditLog.getId());
		auditLogNode.put("tenantId", auditLog.getTenantId());
		if (auditLog.getMerchantId() != null) {
			auditLogNode.put("merchantId", auditLog.getMerchantId());
		}
		auditLogNode.put("entityId", auditLog.getEntityId());
		auditLogNode.put("entityType", auditLog.getEntityType().name());
		auditLogNode.put("userId", auditLog.getUserId());
		auditLogNode.put("userName", auditLog.getUserName());
		auditLogNode.put("actionType", auditLog.getActionType().name());
		if (auditLog.getActionData() != null) {
			auditLogNode.put("actionData", auditLog.getActionData().toString());
		}
		auditLogNode.put("actionStatus", auditLog.getActionStatus().name());
		auditLogNode.put("failureDetail", auditLog.getFailureDetail());
		auditLogNode.put("createdTime", LocalDateTime.ofInstant(Instant.ofEpochMilli(auditLog.getCreatedTime()), ZoneId.of("CTT")).format(DateTimeFormatter.ofPattern(dateFormat)));
		return auditLogNode.toString();
	}

	private ResponseListener responseListener = new ResponseListener() {
		@Override
		public void onSuccess(Response response) {
			log.trace("Elasticsearch sink log action method succeeded. Response result [{}]!", response);
		}

		@Override
		public void onFailure(Exception exception) {
			log.warn("Elasticsearch sink log action method failed!", exception);
		}
	};

	private String getIndexName(String tenantId) {
		String indexName = indexPattern;
		if (indexName.contains(TENANT_PLACEHOLDER) && tenantId != null) {
			indexName = indexName.replace(TENANT_PLACEHOLDER, tenantId);
		}
		if (indexName.contains(DATE_PLACEHOLDER)) {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
			indexName = indexName.replace(DATE_PLACEHOLDER, now.format(formatter));
		}
		return indexName.toLowerCase();
	}
}
