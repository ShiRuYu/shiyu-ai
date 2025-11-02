package com.shiyu.ai.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperPlus;
import com.shiyu.ai.demo.domain.SysTenant;
import com.shiyu.ai.demo.domain.vo.SysTenantVo;

/**
 * 租户Mapper接口
 */
@Mapper
public interface SysTenantMapper extends BaseMapperPlus<SysTenant, SysTenantVo> {

}
