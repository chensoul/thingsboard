package com.chensoul.system.domain.oauth2.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface OAuth2ClientRegistrationTemplateMapper extends BaseMapper<OAuth2ClientRegistrationTemplateEntity> {
    OAuth2ClientRegistrationTemplateEntity findByProviderId(String providerId);
}
