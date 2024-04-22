package org.thingsboard.domain.user.persistence;

import java.util.Set;
import org.thingsboard.common.dao.Dao;
import org.thingsboard.common.dao.TenantEntityDao;
import org.thingsboard.common.dao.page.PageData;
import org.thingsboard.common.dao.page.PageLink;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface UserDao extends Dao<User, Long>, TenantEntityDao {
	User findByEmail(String email);

	PageData<User> findByTenantId( String tenantId,PageLink pageLink);

	PageData<User> findByMerchantIds( Set<Long> merchantIds,PageLink pageLink);

	PageData<User> findByTenantIdsAndAuthority( Set<String> tenantIds, Authority authority,PageLink pageLink);

	PageData<User> findByAuthority( Authority authority,PageLink pageLink);

	PageData<User> findUsersByIds( Set<Long> ids,PageLink pageLink);

	PageData<User> findUsers(PageLink pageLink);

}
