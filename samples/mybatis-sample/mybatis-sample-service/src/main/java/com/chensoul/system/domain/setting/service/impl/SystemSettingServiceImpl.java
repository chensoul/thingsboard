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
package com.chensoul.system.domain.setting.service.impl;

import com.chensoul.json.JacksonUtils;
import com.chensoul.spring.util.ServletUtils;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.notification.channel.mail.MailService;
import com.chensoul.system.domain.notification.channel.sms.SmsService;
import com.chensoul.system.domain.setting.domain.JwtSetting;
import com.chensoul.system.domain.setting.domain.SecuritySetting;
import com.chensoul.system.domain.setting.domain.SystemSetting;
import com.chensoul.system.domain.setting.domain.SystemSettingType;
import com.chensoul.system.domain.setting.mybatis.SystemSettingDao;
import com.chensoul.system.domain.setting.service.JwtSettingValidator;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class SystemSettingServiceImpl implements SystemSettingService {
    private final SystemSettingDao systemSettingDao;
    private final SystemSettingValidator systemSettingValidator;
    private final JwtSettingValidator jwtSettingValidator;
    private final SmsService smsService;
    private final MailService mailService;

    @Override
    public SystemSetting findSystemSettingByType(String tenantId, SystemSettingType type) {
        SystemSetting systemSetting = systemSettingDao.findByType(tenantId, type);
        if (systemSetting == null) {
            systemSetting = new SystemSetting();
            systemSetting.setType(type);
            systemSetting.setTenantId(tenantId);

            if (systemSetting.getType().equals(SystemSettingType.SECURITY)) {
                systemSetting.setExtra(JacksonUtils.convertValue(new SecuritySetting(), JsonNode.class));
            } else if (systemSetting.getType().equals(SystemSettingType.JWT)) {
                systemSetting.setExtra(JacksonUtils.convertValue(new JwtSetting(), JsonNode.class));
            }
        }
        return systemSetting;
    }

    @Override
    public SystemSetting saveSystemSetting(String tenantId, SystemSetting systemSetting) {
        systemSettingValidator.validate(systemSetting);

        if (systemSetting.getTenantId() == null) {
            systemSetting.setTenantId(SYS_TENANT_ID);
        }

        if (systemSetting.getType().equals(SystemSettingType.SECURITY)) {

        } else if (systemSetting.getType().equals(SystemSettingType.JWT)) {
            JwtSetting jwtSetting = JacksonUtils.convertValue(systemSetting.getExtra(), JwtSetting.class);
            jwtSettingValidator.validate(jwtSetting);
            if (jwtSetting.isSigningKeyDefault()) {
                jwtSetting.setTokenSigningKey(Base64.getEncoder().encodeToString(
                    RandomStringUtils.randomAlphanumeric(64).getBytes(StandardCharsets.UTF_8)));
                systemSetting.setExtra(JacksonUtils.valueToTree(jwtSetting));
            }
        } else if (systemSetting.getType().equals(SystemSettingType.EMAIL)) {
            SystemSetting mailSetting = findSystemSettingByType(tenantId, SystemSettingType.EMAIL);
            if (mailSetting != null) {
                JsonNode newJsonValue = systemSetting.getExtra();
                JsonNode oldJsonValue = mailSetting.getExtra();
                if (!newJsonValue.has("password") && oldJsonValue.has("password")) {
                    ((ObjectNode) newJsonValue).put("password", oldJsonValue.get("password").asText());
                }
                if (!newJsonValue.has("refreshToken") && oldJsonValue.has("refreshToken")) {
                    ((ObjectNode) newJsonValue).put("refreshToken", oldJsonValue.get("refreshToken").asText());
                }
                dropTokenIfProviderInfoChanged(newJsonValue, oldJsonValue);
            }

            mailService.updateMailConfiguration();
            ((ObjectNode) systemSetting.getExtra()).remove("password");
            ((ObjectNode) systemSetting.getExtra()).remove("refreshToken");
        } else if (systemSetting.getType().equals(SystemSettingType.SMS)) {
            smsService.updateSmsConfiguration();
        }
        return systemSettingDao.save(systemSetting);
    }

    @Override
    public void deleteSystemSettingByTenantIdAndType(String tenantId, SystemSettingType type) {
        systemSettingDao.removeByTenantIdAndType(tenantId, type);
    }

    @Override
    public void deleteSystemSettingByTenantId(String tenantId) {
        systemSettingDao.removeByTenantId(tenantId);
    }


    @Override
    public String getBaseUrl(HttpServletRequest httpServletRequest) {
        String baseUrl = null;
        SystemSetting generalSettings = findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.GENERAL);

        if (generalSettings != null) {
            JsonNode prohibitDifferentUrl = generalSettings.getExtra().get("prohibitDifferentUrl");

            if ((prohibitDifferentUrl != null && prohibitDifferentUrl.asBoolean())) {
                baseUrl = generalSettings.getExtra().get("baseUrl").asText();
            }
        }

        if (StringUtils.isEmpty(baseUrl) && httpServletRequest != null) {
            baseUrl = ServletUtils.constructBaseUrl(httpServletRequest);
        }

        return baseUrl;
    }

    @Override
    public SecuritySetting getSecuritySetting() {
        SystemSetting systemSetting = findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.SECURITY);
        return JacksonUtils.convertValue(systemSetting.getExtra(), SecuritySetting.class);
    }

    private void dropTokenIfProviderInfoChanged(JsonNode newJsonValue, JsonNode oldJsonValue) {
        if (newJsonValue.has("enableOauth2") && newJsonValue.get("enableOauth2").asBoolean()) {
            if (!newJsonValue.get("providerId").equals(oldJsonValue.get("providerId")) ||
                !newJsonValue.get("clientId").equals(oldJsonValue.get("clientId")) ||
                !newJsonValue.get("clientSecret").equals(oldJsonValue.get("clientSecret")) ||
                !newJsonValue.get("redirectUri").equals(oldJsonValue.get("redirectUri")) ||
                (newJsonValue.has("providerTenantId") && !newJsonValue.get("providerTenantId").equals(oldJsonValue.get("providerTenantId")))) {
                ((ObjectNode) newJsonValue).put("tokenGenerated", false);
                ((ObjectNode) newJsonValue).remove("refreshToken");
                ((ObjectNode) newJsonValue).remove("refreshTokenExpires");
            }
        }
    }

}
