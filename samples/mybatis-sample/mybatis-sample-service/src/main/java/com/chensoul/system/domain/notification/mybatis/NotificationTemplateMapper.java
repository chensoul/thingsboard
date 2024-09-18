package com.chensoul.system.domain.notification.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.system.domain.notification.domain.template.NotificationType;
import java.util.List;
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
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplateEntity> {
    void deleteByTenantId(String tenantId);

    //    @Query("SELECT t FROM NotificationTemplateEntity t WHERE t.tenantId = :tenantId AND " +
//           "t.type IN :notificationTypes " +
//           "AND (:searchText is NULL OR ilike(t.name, concat('%', :searchText, '%')) = true " +
//           "OR ilike(t.type, concat('%', :searchText, '%')) = true)")
    Page<NotificationTemplateEntity> findByTenantIdAndNotificationTypesAndSearchText(Pageable pageable,
                                                                                     @Param("tenantId") String tenantId,
                                                                                     @Param("notificationTypes") List<NotificationType> notificationTypes,
                                                                                     @Param("searchText") String searchText);
}
