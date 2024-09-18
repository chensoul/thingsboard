package com.chensoul.system.domain.oauth2.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.oauth2.domain.OAuth2ClientRegistrationTemplate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
public class OAuth2ClientRegistrationTemplateDao extends AbstractDao<OAuth2ClientRegistrationTemplateEntity, OAuth2ClientRegistrationTemplate, Long> {
  private final OAuth2ClientRegistrationTemplateMapper repository;

  @Override
  protected Class<OAuth2ClientRegistrationTemplateEntity> getEntityClass() {
    return OAuth2ClientRegistrationTemplateEntity.class;
  }

  @Override
  protected BaseMapper<OAuth2ClientRegistrationTemplateEntity> getRepository() {
    return repository;
  }

  public OAuth2ClientRegistrationTemplate findByProviderId(String providerId) {
    return DaoUtil.getData(repository.findByProviderId(providerId));
  }

  public List<OAuth2ClientRegistrationTemplate> findAll() {
    return DaoUtil.convertDataList(repository.selectList(Wrappers.lambdaQuery()));
  }
}
