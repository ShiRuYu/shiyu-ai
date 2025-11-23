package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysTenantDO;
import com.shiyu.ai.auth.domain.bo.SysTenantBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户Mapper接口
 */
@Mapper
public interface SysTenantMapper extends BaseMapperFlex<SysTenantDO, SysTenantBO> {

}
