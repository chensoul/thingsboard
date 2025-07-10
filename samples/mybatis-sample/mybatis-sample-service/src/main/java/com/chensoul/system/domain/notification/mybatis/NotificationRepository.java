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
package com.chensoul.system.domain.notification.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.system.domain.notification.domain.NotificationStatus;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryMethod;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface NotificationRepository extends BaseMapper<NotificationEntity> {

    @Transactional
//    @Query("DELETE FROM NotificationEntity n WHERE n.id = :id AND n.recipientId = :recipientId")
    int deleteByIdAndRecipientId(@Param("id") Long id, @Param("recipientId") Long recipientId);

    @Transactional
//    @Query("UPDATE NotificationEntity n SET n.status = :status " +
//           "WHERE n.id = :id AND n.recipientId = :recipientId AND n.status <> :status")
    int updateStatusByIdAndRecipientId(@Param("id") Long id,
                                       @Param("recipientId") Long recipientId,
                                       @Param("status") NotificationStatus status);

    @Transactional
//    @Query("UPDATE NotificationEntity n SET n.status = :status " +
//           "WHERE n.deliveryMethod = :deliveryMethod AND n.recipientId = :recipientId AND n.status <> :status")
    int updateStatusByDeliveryMethodAndRecipientIdAndStatusNot(@Param("deliveryMethod") NotificationDeliveryMethod deliveryMethod,
                                                               @Param("recipientId") UUID recipientId,
                                                               @Param("status") NotificationStatus status);

    //    @Query("SELECT n FROM NotificationEntity n WHERE n.deliveryMethod = :deliveryMethod " +
//           "AND n.recipientId = :recipientId AND n.status <> :status " +
//           "AND (:searchText is NULL OR ilike(n.subject, concat('%', :searchText, '%')) = true " +
//           "OR ilike(n.text, concat('%', :searchText, '%')) = true)")
    Page<NotificationEntity> findByDeliveryMethodAndRecipientIdAndStatusNot(
        @Param("deliveryMethod") NotificationDeliveryMethod deliveryMethod,
        @Param("recipientId") Long recipientId,
        @Param("status") NotificationStatus status,
        @Param("searchText") String searchText, Pageable pageable);


    //    @Query("SELECT n FROM NotificationEntity n WHERE n.deliveryMethod = :deliveryMethod AND n.recipientId = :recipientId " +
//           "AND (:searchText is NULL OR ilike(n.subject, concat('%', :searchText, '%')) = true " +
//           "OR ilike(n.text, concat('%', :searchText, '%')) = true)")
    Page<NotificationEntity> findByDeliveryMethodAndRecipientId(@Param("deliveryMethod") NotificationDeliveryMethod deliveryMethod,
                                                                @Param("recipientId") Long recipientId,
                                                                @Param("searchText") String searchText, Pageable pageable);
}
