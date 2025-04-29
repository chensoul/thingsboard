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
package com.chensoul.system.domain.oauth2.service.impl;

import com.chensoul.data.event.SaveEntityEvent;
import com.chensoul.data.model.BaseData;
import com.chensoul.exception.BusinessException;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.oauth2.domain.OAuth2ClientInfo;
import com.chensoul.system.domain.oauth2.domain.OAuth2Domain;
import com.chensoul.system.domain.oauth2.domain.OAuth2Info;
import com.chensoul.system.domain.oauth2.domain.OAuth2Mobile;
import com.chensoul.system.domain.oauth2.domain.OAuth2Param;
import com.chensoul.system.domain.oauth2.domain.OAuth2ParamInfo;
import com.chensoul.system.domain.oauth2.domain.OAuth2Registration;
import com.chensoul.system.domain.oauth2.service.OAuth2InfoValidator;
import com.chensoul.system.domain.oauth2.service.OAuth2Service;
import com.chensoul.system.domain.oauth2.OAuth2Utils;
import com.chensoul.system.domain.oauth2.domain.PlatformType;
import com.chensoul.system.domain.oauth2.domain.SchemeType;
import com.chensoul.system.domain.oauth2.mybatis.OAuth2DomainDao;
import com.chensoul.system.domain.oauth2.mybatis.OAuth2MobileDao;
import com.chensoul.system.domain.oauth2.mybatis.OAuth2ParamDao;
import com.chensoul.system.domain.oauth2.mybatis.OAuth2RegistrationDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {
    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_CLIENT_REGISTRATION_ID = "Incorrect clientRegistrationId ";
    public static final String INCORRECT_DOMAIN_NAME = "Incorrect domainName ";
    public static final String INCORRECT_DOMAIN_SCHEME = "Incorrect domainScheme ";

    private final OAuth2RegistrationDao oauth2RegistrationDao;
    private final OAuth2ParamDao oauth2ParamDao;
    private final OAuth2DomainDao oauth2DomainDao;
    private final OAuth2MobileDao oauth2MobileDao;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<OAuth2ClientInfo> getOAuth2Clients(String domainScheme, String domainName, String pkgName, PlatformType platformType) {
        log.trace("Executing getOAuth2Clients [{}://{}] pkgName=[{}] platformType=[{}]", domainScheme, domainName, pkgName, platformType);
        if (domainScheme == null) {
            throw new BusinessException(INCORRECT_DOMAIN_SCHEME);
        }
        SchemeType schemeType;
        try {
            schemeType = SchemeType.valueOf(domainScheme.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(INCORRECT_DOMAIN_SCHEME);
        }
        return oauth2RegistrationDao.findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(
                Arrays.asList(schemeType, SchemeType.MIXED), domainName, pkgName, platformType)
            .stream()
            .map(OAuth2Utils::toClientInfo)
            .collect(Collectors.toList());
    }

    @Override
    public void saveOAuth2Info(OAuth2Info oauth2Info) {
        new OAuth2InfoValidator().accept(oauth2Info);
        oauth2ParamDao.deleteAll();
        oauth2Info.getOauth2ParamInfos().forEach(oauth2ParamsInfo -> {
            OAuth2Param oauth2Param = OAuth2Utils.infoToOAuth2Param(oauth2Info);
            OAuth2Param savedOauth2Param = oauth2ParamDao.save(oauth2Param);
            oauth2ParamsInfo.getClientRegistrations().forEach(registrationInfo -> {
                OAuth2Registration registration = OAuth2Utils.toOAuth2Registration(savedOauth2Param.getId(), registrationInfo);
                oauth2RegistrationDao.save(registration);
            });
            oauth2ParamsInfo.getDomainInfos().forEach(domainInfo -> {
                OAuth2Domain domain = OAuth2Utils.toOAuth2Domain(savedOauth2Param.getId(), domainInfo);
                oauth2DomainDao.save(domain);
            });
            if (oauth2ParamsInfo.getMobileInfos() != null) {
                oauth2ParamsInfo.getMobileInfos().forEach(mobileInfo -> {
                    OAuth2Mobile mobile = OAuth2Utils.toOAuth2Mobile(savedOauth2Param.getId(), mobileInfo);
                    oauth2MobileDao.save(mobile);
                });
            }
        });
        eventPublisher.publishEvent(SaveEntityEvent.builder().entity(oauth2Info).build());
    }

    @Override
    public OAuth2Info findOAuth2Info() {
        OAuth2Info oauth2Info = new OAuth2Info();
        List<OAuth2Param> oauth2ParamsList = oauth2ParamDao.findByTenantId(SYS_TENANT_ID);
        oauth2Info.setEnabled(oauth2ParamsList.stream().anyMatch(OAuth2Param::isEnabled));
        List<OAuth2ParamInfo> oauth2ParamsInfos = new ArrayList<>();
        oauth2Info.setOauth2ParamInfos(oauth2ParamsInfos);
        oauth2ParamsList.stream().sorted(Comparator.comparing(BaseData::getId)).forEach(oAuth2Param -> {
            List<OAuth2Registration> registrations = oauth2RegistrationDao.findByOAuth2ParamId(oAuth2Param.getId());
            List<OAuth2Domain> domains = oauth2DomainDao.findByOAuth2ParamId(oAuth2Param.getId());
            List<OAuth2Mobile> mobiles = oauth2MobileDao.findByOAuth2ParamId(oAuth2Param.getId());
            oauth2ParamsInfos.add(OAuth2Utils.toOAuth2ParamInfo(registrations, domains, mobiles));
        });
        return oauth2Info;
    }

    @Override
    public OAuth2Registration findRegistration(String id) {
        return oauth2RegistrationDao.findById(id);
    }

    @Override
    public List<OAuth2Registration> findAllRegistrations() {
        return oauth2RegistrationDao.find();
    }

    @Override
    public String findAppSecret(String registrationId, String pkgName) {
        return oauth2RegistrationDao.findAppSecret(registrationId, pkgName);
    }
}
