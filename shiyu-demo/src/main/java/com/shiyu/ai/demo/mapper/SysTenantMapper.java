package com.shiyu.ai.demo.mapper;

import com.shiyu.ai.demo.domain.bo.SysTenantBO;
import org.apache.ibatis.annotations.Mapper;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.demo.domain.SysTenantDO;

/**
 * 租户Mapper接口
 */
@Mapper
public interface SysTenantMapper extends BaseMapperFlex<SysTenantDO, SysTenantBO> {

}
