package com.shiyu.ai.agent.dal.mapper;

import com.shiyu.ai.agent.dal.dataobject.RoleDO;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 数据层
 */
@Mapper
public interface RoleMapper extends BaseMapperFlex<RoleDO, RoleBO> {


}
