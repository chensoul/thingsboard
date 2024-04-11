package org.thingsboard.domain.user.persistence;

import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thingsboard.common.dao.Dao;
import org.thingsboard.common.dao.TenantEntityDao;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface UserDao extends Dao<User>, TenantEntityDao {
	User findByEmail(String email);

	Page<User> findTenantAndCustomerUsers(Pageable pageable, Set<String> tenantIds, Set<Long> merchantIds, Set<Long> userIds, Authority authority, String textSearch);
}
