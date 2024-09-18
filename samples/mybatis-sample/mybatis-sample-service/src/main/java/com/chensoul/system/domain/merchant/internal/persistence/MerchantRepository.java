package com.chensoul.system.domain.merchant.internal.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface MerchantRepository extends BaseMapper<MerchantEntity> {
    MerchantEntity findByTenantIdAndName(String tenantId, String name);

    void deleteByTenantId(String tenantId);

    //    @Query("SELECT c FROM MerchantEntity c WHERE c.tenantId = :tenantId " +
//           "AND (:textSearch IS NULL OR ilike(c.name, CONCAT('%', :textSearch, '%')) = true)")
    Page<MerchantEntity> findByTenantId(@Param("tenantId") String tenantId,
                                        @Param("textSearch") String textSearch,
                                        Pageable pageable);
}
