package com.chensoul.system.domain.oauth2.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface OAuth2DomainMapper extends BaseMapper<OAuth2DomainEntity> {
    List<OAuth2DomainEntity> findByOauth2ParamId(Long oauth2ParamId);
}
