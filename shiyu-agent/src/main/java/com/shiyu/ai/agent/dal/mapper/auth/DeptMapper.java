package com.shiyu.ai.agent.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.auth.DeptDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface DeptMapper extends BaseMapperFlex<DeptDO> {

}
