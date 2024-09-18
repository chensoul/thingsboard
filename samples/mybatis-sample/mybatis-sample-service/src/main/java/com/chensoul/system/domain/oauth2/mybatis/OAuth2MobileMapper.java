package com.chensoul.system.domain.oauth2.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.model.ToData;
import com.chensoul.system.domain.oauth2.domain.OAuth2Mobile;
import java.util.Collection;
import org.apache.ibatis.annotations.Mapper;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface OAuth2MobileMapper extends BaseMapper<OAuth2MobileEntity> {
    Collection<? extends ToData<OAuth2Mobile>> findByOauth2ParamId(Long oauth2ParamId);
}
