package com.shiyu.ai.dal.auth.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.auth.dataobject.WorkspaceDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Workspace 接口
 */

/**
 * 工作空间表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface WorkspaceMapper extends BaseMapperFlex<WorkspaceDO> {

}
