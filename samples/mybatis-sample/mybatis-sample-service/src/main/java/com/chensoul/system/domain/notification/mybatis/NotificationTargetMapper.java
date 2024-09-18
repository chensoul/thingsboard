package com.chensoul.system.domain.notification.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface NotificationTargetMapper extends BaseMapper<NotificationTargetEntity> {
    void deleteByTenantId(String tenantId);

    Long countByTenantId(String tenantId);

    List<NotificationTargetEntity> findByTenantIdAndIdIn(String tenantId, Set<Long> ids);

    //    @Query(value = "SELECT * FROM notification_target t WHERE t.tenant_id = :tenantId " +
//                   "AND (:searchText IS NULL OR t.name ILIKE concat('%', :searchText, '%')) " +
//                   "AND (cast(t.configuration as json) ->> 'type' <> 'PLATFORM_USERS' OR " +
//                   "cast(t.configuration as json) -> 'usersFilter' ->> 'type' IN :usersFilterTypes)", nativeQuery = true)
    Page<NotificationTargetEntity> findByTenantIdAndSearchTextAndUsersFilterTypeIfPresent(@Param("tenantId") String tenantId,
                                                                                          @Param("usersFilterTypes") List<String> usersFilterTypes,
                                                                                          @Param("searchText") String searchText,
                                                                                          Pageable pageable);

}
