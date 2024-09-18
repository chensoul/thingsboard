package com.chensoul.system.domain.user.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.user.domain.Authority;
import com.chensoul.system.user.domain.User;
import java.util.Set;
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
public class UserDao extends AbstractDao<UserEntity, User, Long> {
    private final UserMapper mapper;

    @Override
    protected Class<UserEntity> getEntityClass() {
        return UserEntity.class;
    }

    @Override
    protected BaseMapper<UserEntity> getRepository() {
        return mapper;
    }

    public User findByEmail(String email) {
        return DaoUtil.getData(mapper.selectOne(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getEmail, email)));
    }

    public PageData<User> findByMerchantIds(Set<Long> merchantIds, PageLink pageLink) {
        return DaoUtil.toPageData(mapper.findByMerchantIds(merchantIds, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }

    public PageData<User> findByTenantId(String tenantId, PageLink pageLink) {
        return DaoUtil.toPageData(mapper.findByTenantId(tenantId, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }

    public PageData<User> findUsersByIds(Set<Long> ids, PageLink pageLink) {
        return DaoUtil.toPageData(mapper.findByIds(ids, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }

    public PageData<User> findUsers(PageLink pageLink) {
        return DaoUtil.toPageData(mapper.find(pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }

    public PageData<User> findByTenantIdsAndAuthority(Set<String> tenantIds, Authority authority, PageLink pageLink) {
        return DaoUtil.toPageData(mapper.findByTenantIdsAndAuthority(tenantIds, authority, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }

    public PageData<User> findByAuthority(Authority authority, PageLink pageLink) {
        return DaoUtil.toPageData(mapper.findByAuthority(authority, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }
}
