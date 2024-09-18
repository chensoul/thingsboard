package com.chensoul.system.domain.tenant.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface TenantProfileMapper extends BaseMapper<TenantProfileEntity> {
//    @Query("SELECT t FROM TenantProfileEntity t " +
//           "WHERE t.defaulted = true")
//    TenantProfileEntity findByDefaultTrue();
//
//    @Query("SELECT t FROM TenantProfileEntity t WHERE " +
//           "(:textSearch IS NULL OR ilike(t.name, CONCAT('%', :textSearch, '%')) = true)")
//    Page<TenantProfileEntity> findTenantProfiles(@Param("textSearch") String textSearch, Pageable pageable);

}
