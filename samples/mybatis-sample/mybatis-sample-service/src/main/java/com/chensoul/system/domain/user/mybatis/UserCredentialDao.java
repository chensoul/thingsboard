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
