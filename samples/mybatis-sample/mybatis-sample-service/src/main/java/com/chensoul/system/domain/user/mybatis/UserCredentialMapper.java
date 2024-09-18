package com.chensoul.system.domain.user.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface UserCredentialMapper extends BaseMapper<UserCredentialEntity> {
    @Select("select * from user_credential where user_id = #{userId}")
    UserCredentialEntity findByUserId(Long userId);

}
