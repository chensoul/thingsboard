package com.chensoul.system.domain.user.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.system.user.domain.Authority;
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
public interface UserMapper extends BaseMapper<UserEntity> {
    //    @Query("SELECT u FROM UserEntity u WHERE u.tenantId IN (:tenantIds)  AND u.authority = :authority " +
//           "AND (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
    Page<UserEntity> findByTenantIdsAndAuthority(@Param("tenantIds") Set<String> tenantIds,
                                                 @Param("authority") Authority authority,
                                                 @Param("searchText") String searchText,
                                                 Pageable pageable);

    //    @Query("SELECT u FROM UserEntity u WHERE u.authority = :authority " +
//           "And (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
    Page<UserEntity> findByAuthority(@Param("authority") Authority authority,
                                     @Param("searchText") String searchText,
                                     Pageable pageable);

    //    @Query("SELECT u FROM UserEntity u WHERE u.tenantId = :tenantId " +
//           "AND (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
    Page<UserEntity> findByTenantId(
        @Param("tenantId") String tenantId,
        @Param("searchText") String searchText, Pageable pageable);

    //    @Query("SELECT u FROM UserEntity u WHERE u.merchantId in (:merchantIds) " +
//           "AND (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
    Page<UserEntity> findByMerchantIds(@Param("merchantIds") Set<Long> merchantIds,
                                       @Param("searchText") String searchText, Pageable pageable);

    //    @Query("SELECT u FROM UserEntity u WHERE u.id IN (:ids) " +
//           "AND (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
    Page<UserEntity> findByIds(Set<Long> ids, String searchText, Pageable pageable);

    //    @Query("SELECT u FROM UserEntity u WHERE (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
    Page<UserEntity> find(String searchText, Pageable pageable);
}
