package com.shiyu.ai.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.auth.TenantDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * Tenant 接口
 */

public interface TenantMapper extends BaseMapperFlex<TenantDO> {
}
