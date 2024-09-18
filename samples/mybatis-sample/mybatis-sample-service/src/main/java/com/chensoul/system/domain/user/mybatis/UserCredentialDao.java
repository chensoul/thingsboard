package com.chensoul.system.domain.user.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chensoul.system.user.domain.UserCredential;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Component
@RequiredArgsConstructor
public class UserCredentialDao extends AbstractDao<UserCredentialEntity, UserCredential, Long> {
    private final UserCredentialMapper mapper;

    @Override
    protected Class<UserCredentialEntity> getEntityClass() {
        return UserCredentialEntity.class;
    }

    @Override
    protected BaseMapper<UserCredentialEntity> getRepository() {
        return mapper;
    }

    public UserCredential findByUserId(Long userId) {
        return DaoUtil.getData(mapper.findByUserId(userId));
    }

    public void removeByUserId(Long userId) {
        mapper.delete(Wrappers.<UserCredentialEntity>lambdaQuery().eq(UserCredentialEntity::getUserId, userId));
    }

    public UserCredential findByActivateToken(String activateToken) {
        return DaoUtil.getData(mapper.selectOne(Wrappers.<UserCredentialEntity>lambdaQuery().eq(UserCredentialEntity::getActivateToken, activateToken)));
    }

    public UserCredential findByResetToken(String resetToken) {
        return DaoUtil.getData(mapper.selectOne(Wrappers.<UserCredentialEntity>lambdaQuery().eq(UserCredentialEntity::getResetToken, resetToken)));
    }

}
