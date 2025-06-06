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
///**
// * Copyright © 2016-2024 The Thingsboard Authors
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.chensoul.cloud.system.audit.sink;
//
//import com.chensoul.cloud.system.audit.service.AuditLog;
//import com.chensoul.json.JacksonUtils;
//import com.chensoul.util.concurrent.GroupedThreadFactory;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpHost;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.CredentialsProvider;
//import org.apache.http.entity.ContentType;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.apache.http.nio.entity.NStringEntity;
//import org.elasticsearch.client.Request;
//import org.elasticsearch.client.Response;
//import org.elasticsearch.client.ResponseListener;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestClientBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.http.HttpMethod;
//import org.springframework.stereotype.Component;
//
//@Component
//@ConditionalOnProperty(prefix = "audit-log.sink", value = "type", havingValue = "elasticsearch")
//@Slf4j
//public class ElasticsearchAuditLogSink implements AuditLogSink {
//    private static final String TENANT_PLACEHOLDER = "@{TENANT}";
//    private static final String DATE_PLACEHOLDER = "@{DATE}";
//    private static final String INDEX_TYPE = "audit_log";
//
//    @Value("${audit-log.sink.index_pattern}")
//    private String indexPattern;
//    @Value("${audit-log.sink.scheme_name}")
//    private String schemeName;
//    @Value("${audit-log.sink.host}")
//    private String host;
//    @Value("${audit-log.sink.port}")
//    private int port;
//    @Value("${audit-log.sink.user_name}")
//    private String userName;
//    @Value("${audit-log.sink.password}")
//    private String password;
//    @Value("${audit-log.sink.date_format}")
//    private String dateFormat;
//
//    private RestClient restClient;
//    private ExecutorService executor;
//    private ResponseListener responseListener = new ResponseListener() {
//        @Override
//        public void onSuccess(Response response) {
//            log.trace("Elasticsearch sink log action method succeeded. Response result [{}]!", response);
//        }
//
//        @Override
//        public void onFailure(Exception exception) {
//            log.warn("Elasticsearch sink log action method failed!", exception);
//        }
//    };
//
//    @PostConstruct
//    public void init() {
//        try {
//            log.info("Adding elastic rest endpoint... host [{}], port [{}], scheme name [{}]",
//                host, port, schemeName);
//            RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, schemeName));
//
//            if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
//                log.info("...using username [{}] and password ***", userName);
//                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//                credentialsProvider.setCredentials(AuthScope.ANY,
//                    new UsernamePasswordCredentials(userName, password));
//                builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
//            }
//
//            this.restClient = builder.build();
//            this.executor = Executors.newSingleThreadExecutor(GroupedThreadFactory.forName("elasticsearch-audit-log"));
//        } catch (Exception e) {
//            log.error("Sink init failed!", e);
//            throw new RuntimeException(e.getMessage(), e);
//        }
//    }
//
//    @PreDestroy
//    private void destroy() {
//        if (executor != null) {
//            executor.shutdownNow();
//        }
//    }
//
//    @Override
//    public void logAction(AuditLog auditLog) {
//        executor.execute(() -> {
//            try {
//                doLogAction(auditLog);
//            } catch (Exception e) {
//                log.error("Failed to log action", e);
//            }
//        });
//    }
//
//    private void doLogAction(AuditLog auditLogEntry) {
//        String jsonContent = createElasticJsonRecord(auditLogEntry);
//
//        HttpEntity entity = new NStringEntity(
//            jsonContent,
//            ContentType.APPLICATION_JSON);
//
//        Request request = new Request(HttpMethod.POST.name(), String.format("/%s/%s", getIndexName(auditLogEntry.getTenantId()), INDEX_TYPE));
//        request.setEntity(entity);
//
//        restClient.performRequestAsync(request, responseListener);
//    }
//
//    private String createElasticJsonRecord(AuditLog auditLog) {
//        ObjectNode auditLogNode = JacksonUtils.newObjectNode();
////		auditLogNode.put("postDate", LocalDateTime.now().toString());
//        auditLogNode.put("id", auditLog.getId());
//        auditLogNode.put("tenantId", auditLog.getTenantId());
//        if (auditLog.getMerchantId() != null) {
//            auditLogNode.put("merchantId", auditLog.getMerchantId());
//        }
//        auditLogNode.put("entityId", auditLog.getEntityId());
//        auditLogNode.put("entityType", auditLog.getEntityType().name());
//        auditLogNode.put("userId", auditLog.getUserId());
//        auditLogNode.put("userName", auditLog.getUserName());
//        auditLogNode.put("actionType", auditLog.getActionType().name());
//        if (auditLog.getExtra() != null) {
//            auditLogNode.put("extra", auditLog.getExtra().toString());
//        }
//        auditLogNode.put("actionStatus", auditLog.getActionStatus().name());
//        auditLogNode.put("failureDetail", auditLog.getFailureDetail());
//        auditLogNode.put("createdTime", auditLog.getCreateTime().format(DateTimeFormatter.ofPattern(dateFormat)));
//        return auditLogNode.toString();
//    }
//
//    private String getIndexName(String tenantId) {
//        String indexName = indexPattern;
//        if (indexName.contains(TENANT_PLACEHOLDER) && tenantId != null) {
//            indexName = indexName.replace(TENANT_PLACEHOLDER, tenantId);
//        }
//        if (indexName.contains(DATE_PLACEHOLDER)) {
//            LocalDateTime now = LocalDateTime.now();
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
//            indexName = indexName.replace(DATE_PLACEHOLDER, now.format(formatter));
//        }
//        return indexName.toLowerCase();
//    }
//}
