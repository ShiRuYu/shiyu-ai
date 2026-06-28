package com.shiyu.ai.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RoleMapper extends BaseMapperFlex<RoleDO> {

}
